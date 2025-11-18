package com.pcgear.complink.pcgear.Item;

import java.util.List;

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

        @Cacheable(value = "items", condition = "#p0 == null", key = "#p1.toString()")
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

        @Transactional
        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
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

        @Transactional
        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        public void deleteItems(List<Integer> itemIds) {
                itemRepository.deleteAllByItemIdIn(itemIds);
        }

        @CacheEvict(value = { "items", "items_temp" }, allEntries = true)
        public void updateItemQuantityOnHand(Order order) {

                List<OrderItem> itemsFromOrder = order.getOrderItems();

                for (OrderItem orderItem : itemsFromOrder) {
                        log.info("orderItem: {}", orderItem);
                        Item item = orderItem.getItem();

                        int orderedQuantity = orderItem.getQuantity();
                        int currentStock = item.getQuantityOnHand();

                        if (currentStock < orderedQuantity) {
                                throw new IllegalStateException(
                                                "재고 부족: 품목 '" + item.getItemName() + "'의 재고가 충분하지 않습니다. (현재 재고: "
                                                                + currentStock + ", 주문 수량: " + orderedQuantity + ")");
                        }

                        item.setQuantityOnHand(currentStock - orderedQuantity);
                        itemRepository.save(item);
                }
        }

        public void updateItemAvailableQuantity(Order order) {

                List<OrderItem> itemsFromOrder = order.getOrderItems();

                for (OrderItem orderItem : itemsFromOrder) {
                        log.info("orderItem: {}", orderItem);
                        Item item = orderItem.getItem();

                        int orderedQuantity = orderItem.getQuantity();
                        int currentAvailableQuantity = item.getAvailableQuantity();

                        if (currentAvailableQuantity < orderedQuantity) {
                                throw new IllegalStateException(
                                                "가용 재고 부족: 품목 '" + item.getItemName()
                                                                + "'의 가용 재고가 충분하지 않습니다. (현재 가용 재고: "
                                                                + currentAvailableQuantity + ", 주문 수량: "
                                                                + orderedQuantity
                                                                + ")");
                        }

                        item.setAvailableQuantity(currentAvailableQuantity - orderedQuantity);
                        itemRepository.save(item);
                }
        }

        

        public void restoreItemAvailableQuantity(Integer orderId) {
                Order order = orderRepository.findById(orderId).orElseThrow(()-> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));
                List<OrderItem> itemsFromOrder = order.getOrderItems();
                
                for (OrderItem orderItem : itemsFromOrder) {
                        Item item= orderItem.getItem();
                        item.setAvailableQuantity(item.getAvailableQuantity()+orderItem.getQuantity());
                        itemRepository.save(item);
                }
        }
}
