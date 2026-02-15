package com.pcgear.complink.pcgear.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.model.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, OrderRepositoryCustom {
        boolean existsByCustomerCustomerId(String customerId);

        List<Order> findByOrderStatus(OrderStatus orderStatus);

        Optional<Order> findByMerchantUid(String merchantUid);

        List<Order> findAllByOrderStatusIn(List<OrderStatus> orderStatuses);

        // 단순 카운트 쿼리 (Spring Data JPA 메서드 네이밍)
        Integer countByOrderStatus(OrderStatus status);

        Integer countByOrderStatusIn(List<OrderStatus> statuses);

        // 복잡한 쿼리들은 OrderRepositoryCustom(QueryDSL)로 이전됨:
        // - getTodayNewOrdersCount
        // - findByIdWithItemsAndCustomer
        // - findAssemblyQueue (기존 findAllByOrderStatusIn 페이징 버전)
        // - findShippingList
        // - findAllWithFetchJoin
        // - findByOrderIdWithFetchJoin

}
