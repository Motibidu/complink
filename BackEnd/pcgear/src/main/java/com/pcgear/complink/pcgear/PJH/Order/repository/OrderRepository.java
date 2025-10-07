package com.pcgear.complink.pcgear.PJH.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    boolean existsByCustomerCustomerId(String customerId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    Optional<Order> findByMerchantUid(String merchantUid);
}
