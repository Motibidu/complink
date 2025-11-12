package com.pcgear.complink.pcgear.Manager;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Item.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {
        private final ManagerRepository managerRepository;

        public Manager createManager(Manager manager) {
                return managerRepository.save(manager);
        }

        @Transactional // 데이터 변경이 있으므로 트랜잭션을 보장합니다.
        public void updateManager(String managerId, Manager managerDetails) {
                Manager existingManager = managerRepository.findById(managerId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 담당자를 찾을 수 없습니다: " + managerId));

                existingManager.setManagerName(managerDetails.getManagerName());
                existingManager.setEmail(managerDetails.getEmail());
                existingManager.setPhoneNumber(managerDetails.getPhoneNumber());

                managerRepository.save(existingManager);
        }

        @Transactional
        public void deleteManagers(List<String> managerIds) {
                managerRepository.deleteAllByManagerIdIn(managerIds);
        }

        public Page<Manager> getAllManagers(Pageable pageable) {
                Page<Manager> customerPage = managerRepository.findAll(pageable);
                return customerPage;
        }

}
