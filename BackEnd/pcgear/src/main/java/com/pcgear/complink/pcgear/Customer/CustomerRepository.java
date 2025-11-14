package com.pcgear.complink.pcgear.Customer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    void deleteAllByCustomerIdIn(List<String> customerIds);

    Page<Customer> findByCustomerNameContaining(String search, Pageable pageable);
}
