package com.pcgear.complink.pcgear.Item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderItem;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
        private final ItemRepository itemRepository;
        private final OrderRepository orderRepository;

        // @Cacheable(value = "items", condition = "#p0 == null", key =
        // "#p1.toString()")
        public ItemPageDto getAllItems(String search, Pageable pageable) {

                if (search != null && !search.isBlank()) {
                        log.info("getAllItems");
                        Page<Item> itemPage = itemRepository.findByItemNameContaining(search, pageable);
                        return new ItemPageDto(itemPage);
                } else {
                        log.info("getAllItems");
                        Page<Item> itemPage = itemRepository.findAll(pageable);
                        return new ItemPageDto(itemPage);

                }
        }

        @Cacheable("items_temp")
        public List<Item> findFirstThreeItems() {
                PageRequest pageable = PageRequest.of(0, 3, Sort.by("itemId").ascending());
                return itemRepository.findAll(pageable).getContent();
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        public Item createItem(Item item) {
                return itemRepository.save(item);
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void updateItem(Integer itemId, Item itemDetails) {
                Item existingItem = itemRepository.findById(itemId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 품목을 찾을 수 없습니다: " + itemId));

                // 2. 조회된 엔티티의 필드를 전달받은 데이터로 업데이트합니다.
                existingItem.setItemName(itemDetails.getItemName());
                existingItem.setItemCategory(itemDetails.getItemCategory());
                existingItem.setPurchasePrice(itemDetails.getPurchasePrice());
                existingItem.setSellingPrice(itemDetails.getSellingPrice());

                itemRepository.save(existingItem);
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void deleteItems(List<Integer> itemIds) {
                itemRepository.deleteAllByItemIdIn(itemIds);
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void updateItemQuantityOnHand(Order order) {

                List<Integer> itemIds = order.getOrderItems().stream()
                                .map(orderItem -> orderItem.getItem().getItemId())
                                .collect(Collectors.toList());

                if (itemIds.isEmpty())
                        return;

                List<Item> items = itemRepository.findAllByItemIdInWithLock(itemIds);

                Map<Integer, Item> itemMap = items.stream()
                                .collect(Collectors.toMap(Item::getItemId, item -> item));

                for (OrderItem orderItem : order.getOrderItems()) {
                        log.info("orderItem: {}", orderItem);
                        Item item = itemMap.get(orderItem.getItem().getItemId());

                        int orderedQuantity = orderItem.getQuantity();
                        int currentQuantity = item.getQuantityOnHand();

                        if (currentQuantity < orderedQuantity) {
                                throw new IllegalStateException(
                                                "재고 부족: 품목 '" + item.getItemName() + "'의 재고가 충분하지 않습니다. (현재 재고: "
                                                                + currentQuantity + ", 주문 수량: " + orderedQuantity
                                                                + ")");
                        }

                        item.setQuantityOnHand(currentQuantity - orderedQuantity);
                        itemRepository.save(item);
                }
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void updateItemAvailableQuantity(Order order) {
                log.info("가용재고 차감");

                // 1. N+1문제를 발생시키는 엔티티인 items의 ID를 모두 가져온다.
                List<Integer> itemIds = order.getOrderItems().stream()
                                .map(orderItem -> orderItem.getItem().getItemId())
                                .collect(Collectors.toList());

                if (itemIds.isEmpty())
                        return;

                // 2. 락과 함께 아이템 데이터를 전부 가져온다.
                List<Item> items = itemRepository.findAllByItemIdInWithLock(itemIds);

                Map<Integer, Item> itemMap = items.stream()
                                .collect(Collectors.toMap(Item::getItemId, item -> item));

                // 3. Item만 따로 가져왔기 때문에 OrderItem의 itemId와 itemId가 같은 item을 찾아야 하는 문제가 발생합니다.
                // 3-1. 조회 시간복잡도가 O(1)인 Map을 사용합니다.
                for (OrderItem orderItem : order.getOrderItems()) {
                        Item item = itemMap.get(orderItem.getItem().getItemId());

                        int orderedQuantity = orderItem.getQuantity();
                        int currentAvailableQuantity = item.getAvailableQuantity();

                        if (currentAvailableQuantity < orderedQuantity) {
                                throw new IllegalStateException(
                                                "가용 재고 부족: 품목 '" + item.getItemName() + "' (현재: "
                                                                + currentAvailableQuantity + ", 주문: " + orderedQuantity
                                                                + ")");
                        }

                        // 재고 감소 (Dirty Checking 대기 중)
                        item.setAvailableQuantity(currentAvailableQuantity - orderedQuantity);
                }
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void restoreItemQuantityOnHand(Integer orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

                List<OrderItem> itemsFromOrder = order.getOrderItems();

                // 1. 아이템 ID 목록 추출 (쿼리 X)
                List<Integer> itemIds = itemsFromOrder.stream()
                                .map(orderItem -> orderItem.getItem().getItemId())
                                .collect(Collectors.toList());

                if (itemIds.isEmpty())
                        return;

                // 2. [핵심] 락 걸고 한 방에 조회 (SELECT 1번)
                List<Item> items = itemRepository.findAllByItemIdInWithLock(itemIds);

                // 3. Map으로 변환 (검색 속도 O(1))
                Map<Integer, Item> itemMap = items.stream()
                                .collect(Collectors.toMap(Item::getItemId, item -> item));

                // 4. 재고 복구 로직
                for (OrderItem orderItem : itemsFromOrder) {
                        // 락 걸린 최신 엔티티 가져오기
                        Item item = itemMap.get(orderItem.getItem().getItemId());

                        // 재고 원복 (+Quantity)
                        item.setQuantityOnHand(item.getQuantityOnHand() + orderItem.getQuantity());
                }

                // 5. [저장] save() 호출 불필요 (더티 체킹으로 트랜잭션 종료 시 자동 UPDATE)
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void restoreItemAvailableQuantity(Integer orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

                List<OrderItem> itemsFromOrder = order.getOrderItems();

                // 1. 아이템 ID 목록 추출
                List<Integer> itemIds = itemsFromOrder.stream()
                                .map(orderItem -> orderItem.getItem().getItemId())
                                .collect(Collectors.toList());

                if (itemIds.isEmpty())
                        return;

                // 2. 락 걸고 조회
                List<Item> items = itemRepository.findAllByItemIdInWithLock(itemIds);

                // 3. Map으로 변환 (시간복잡도 O(1))
                Map<Integer, Item> itemMap = items.stream()
                                .collect(Collectors.toMap(Item::getItemId, item -> item));

                // 4. 재고 복구 로직
                for (OrderItem orderItem : itemsFromOrder) {
                        Item item = itemMap.get(orderItem.getItem().getItemId());

                        // 재고 회복 (+Quantity)
                        item.setAvailableQuantity(item.getAvailableQuantity() + orderItem.getQuantity());
                }

                // 5. 더티체킹으로 트랜잭션 종료 시 자동 UPDATE
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        @Transactional
        public void adjustInventory(Integer itemId, int newRealQuantity, String reason) {
                // 1. 락 걸고 조회
                Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                                .orElseThrow(() -> new EntityNotFoundException("품목을 찾을 수 없습니다. ID: " + itemId));

                // 2. 차이 계산 (새 재고 - 현재 재고)
                int oldRealQuantity = item.getQuantityOnHand();
                int diff = newRealQuantity - oldRealQuantity;

                // 3. 실제 재고(QOH) 수정
                item.setQuantityOnHand(newRealQuantity);

                // 4. 가용 재고(ATP)도 차이만큼 자동 반영 (핵심!)
                // (실제 재고가 3개 늘면, 팔 수 있는 것도 3개 늘어야 함)
                item.setAvailableQuantity(item.getAvailableQuantity() + diff);

                log.info("재고 조정 완료: ItemId={}, Old={}, New={}, Reason={}", itemId, oldRealQuantity, newRealQuantity,
                                reason);
        }
}
