package com.pcgear.complink.pcgear.PJH.Order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pcgear.complink.pcgear.PJH.Order.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    void deleteAllByCustomerIdIn(List<String> customerIds);
}
