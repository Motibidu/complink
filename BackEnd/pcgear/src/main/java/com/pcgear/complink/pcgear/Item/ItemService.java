package com.pcgear.complink.pcgear.Item;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
        private final ItemRepository itemRepository;

        public List<Item> readItems() {
                return itemRepository.findAll();
        }

        public Item createItem(Item item) {
                return itemRepository.save(item);
        }

        @Transactional
        public void updateItem(Integer itemId, Item itemDetails) {
                Item existingItem = itemRepository.findById(itemId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 품목을 찾을 수 없습니다: " + itemId));

                // 2. 조회된 엔티티의 필드를 전달받은 데이터로 업데이트합니다.
                existingItem.setItemName(itemDetails.getItemName());
                existingItem.setCategory(itemDetails.getCategory());
                existingItem.setPurchasePrice(itemDetails.getPurchasePrice());
                existingItem.setSellingPrice(itemDetails.getSellingPrice());

                // 3. 변경된 엔티티를 저장합니다.
                // @Transactional 안에서는 변경 감지(Dirty Checking)에 의해 save를 호출하지 않아도
                // 메서드가 끝날 때 자동으로 UPDATE 쿼리가 실행되지만, 명시적으로 호출하는 것도 좋은 방법입니다.
                itemRepository.save(existingItem);
        }

        @Transactional
        public void deleteItems(List<Integer> itemIds) {
                itemRepository.deleteAllByItemIdIn(itemIds);
        }

}
