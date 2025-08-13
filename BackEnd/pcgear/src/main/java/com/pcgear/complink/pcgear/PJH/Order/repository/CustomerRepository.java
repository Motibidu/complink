package com.pcgear.complink.pcgear.PJH.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pcgear.complink.pcgear.PJH.Order.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // UserRepository와 마찬가지로, 기본적인 CRUD 메소드가 자동으로 구현됩니다.
}
