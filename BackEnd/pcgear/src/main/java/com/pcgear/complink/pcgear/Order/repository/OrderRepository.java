package com.pcgear.complink.pcgear.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        /**
         * 특정 품목의 예약된 수량 계산 (재고 정합성 검증용)
         * 주문 접수부터 배송 중까지의 모든 주문에서 해당 품목의 총 수량을 합산
         */
        @Query("""
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM Order o
                JOIN o.orderItems oi
                WHERE oi.item.itemId = :itemId
                AND o.orderStatus IN :statuses
                """)
        Integer calculateReservedQuantityByItemId(
                        @Param("itemId") Integer itemId,
                        @Param("statuses") List<OrderStatus> statuses);

        // 복잡한 쿼리들은 OrderRepositoryCustom(QueryDSL)로 이전됨:
        // - getTodayNewOrdersCount
        // - findByIdWithItemsAndCustomer
        // - findAssemblyQueue (기존 findAllByOrderStatusIn 페이징 버전)
        // - findShippingList
        // - findAllWithFetchJoin
        // - findByOrderIdWithFetchJoin

}
