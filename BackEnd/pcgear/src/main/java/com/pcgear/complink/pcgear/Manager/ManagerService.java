package com.pcgear.complink.pcgear.Manager;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Item.ItemRepository;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {
        private final ManagerRepository managerRepository;
        private final UserRepository userRepository;

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

        // public Page<Manager> getAllManagers(String search, Pageable pageable) {
        // if(search!=null && !search.isEmpty()){
        // Page<Manager> managerPage =
        // managerRepository.findByManagerNameContaining(search, pageable);
        // return managerPage;
        // }
        // else{
        // Page<Manager> managerPage = managerRepository.findAll(pageable);
        // return managerPage;
        // }
        // }

        public Page<UserEntity> getAllManagers(String search, Pageable pageable) {
                if (search != null && !search.isEmpty()) {
                        Page<UserEntity> managerPage = userRepository.findByNameContaining(search, pageable);
                        return managerPage;
                } else {
                        Page<UserEntity> managerPage = userRepository.findAll(pageable);
                        return managerPage;
                }
        }

}
