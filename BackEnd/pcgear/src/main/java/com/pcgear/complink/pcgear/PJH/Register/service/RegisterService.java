package com.pcgear.complink.pcgear.PJH.Register.service;

import java.util.List;

import org.aspectj.lang.annotation.RequiredTypes;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.PJH.Order.model.Customer;
import com.pcgear.complink.pcgear.PJH.Order.model.Manager;
import com.pcgear.complink.pcgear.PJH.Order.repository.CustomerRepository;
import com.pcgear.complink.pcgear.PJH.Order.repository.ManagerRepository;
import com.pcgear.complink.pcgear.PJH.Register.model.Item;
import com.pcgear.complink.pcgear.PJH.Register.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterService {
        private final CustomerRepository customerRepository;
        private final ItemRepository itemRepository;
        private final ManagerRepository managerRepository;

        public void registerCustomer(Customer customer) {
                customerRepository.save(customer);
        }

        public void registerItem(Item item) {

                itemRepository.save(item);
        }

        public void registerManager(Manager manager) {
                managerRepository.save(manager);
        }

        @Transactional
        public void editItem(Integer itemId, Item itemDetails) {
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

        @Transactional // 데이터 변경이 있으므로 트랜잭션을 보장합니다.
        public void editManager(String managerId, Manager managerDetails) {
                // 1. ID로 기존 담당자 정보를 데이터베이스에서 조회합니다.
                Manager existingManager = managerRepository.findById(managerId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 담당자를 찾을 수 없습니다: " + managerId));

                // 2. 조회된 엔티티의 필드를 전달받은 데이터로 업데이트합니다.
                existingManager.setManagerName(managerDetails.getManagerName());
                existingManager.setEmail(managerDetails.getEmail());
                existingManager.setPhoneNumber(managerDetails.getPhoneNumber());

                // 3. 변경된 엔티티를 저장합니다.
                // @Transactional 안에서는 변경 감지(Dirty Checking)에 의해 save를 호출하지 않아도
                // 메서드가 끝날 때 자동으로 UPDATE 쿼리가 실행되지만, 명시적으로 호출하는 것도 좋은 방법입니다.
                managerRepository.save(existingManager);
        }

        @Transactional
        public void deleteManagers(List<String> managerIds) {
                managerRepository.deleteAllByManagerIdIn(managerIds);
        }

        @Transactional
        public void deleteItems(List<Integer> itemIds) {
                itemRepository.deleteAllByItemIdIn(itemIds);
        }

        @Transactional
        public void deleteCustomers(List<String> customerIds) {
                customerRepository.deleteAllByCustomerIdIn(customerIds);
        }

}
