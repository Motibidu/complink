package com.pcgear.complink.pcgear.PJH.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByCustomerCustomerId(String customerId);

    List<Order> findByStatus(String status);
}
