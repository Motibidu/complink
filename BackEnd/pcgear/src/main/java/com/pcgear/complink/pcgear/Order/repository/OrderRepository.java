package com.pcgear.complink.pcgear.Order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, OrderRepositoryCustom {
        boolean existsByCustomerCustomerId(String customerId);

        List<Order> findByOrderStatus(OrderStatus orderStatus);

        Optional<Order> findByMerchantUid(String merchantUid);

        List<Order> findAllByOrderStatusIn(List<OrderStatus> orderStatuses);

        // @Param 어노테이션을 사용하여 :orderId와 매개변수 orderId를 명시적으로 연결해야 합니다.
        @Query("SELECT DISTINCT o FROM Order o " +
                        "JOIN FETCH o.orderItems oi " +
                        "JOIN FETCH o.customer c " +
                        "WHERE o.orderId = :orderId")
        Optional<Order> findByIdWithItemsAndCustomer(@Param("orderId") Integer orderId);

        @Query("SELECT COUNT(o) FROM Order o " +
                        "WHERE o.createdAt BETWEEN :startOfDay AND :endOfDay")
        Integer getNewOrdersToday(
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
        Integer countByOrderStatus(@Param("status") OrderStatus status);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus IN :statuses")
        Integer countByOrderStatusIn(@Param("statuses") List<OrderStatus> statuses);

        @Query("SELECT new com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto(" +
                        " o.orderId, " +
                        " c.customerName," +
                        " m.name," +
                        " o.orderStatus," +
                        " o.assemblyStatus," +
                        " o.paidAt)" +
                        " FROM Order o " +
                        "JOIN o.customer c " +
                        "JOIN o.manager m " +
                        "WHERE o.orderStatus IN :orderStatuses")
        Page<AssemblyQueueRespDto> findAllByOrderStatusIn(@Param("orderStatuses") List<OrderStatus> orderStatuses,
                        Pageable pageable);

        @Query("SELECT new com.pcgear.complink.pcgear.Delivery.model.ShippingListDto(" +
                        " o.orderId, " +
                        " o.customer.customerName, " + // Order -> Customer JOIN
                        " d.createdAt, " +
                        " d.trackingNumber, " + // Order -> Delivery JOIN (수동)
                        " d.carrierId, " +
                        " d.deliveryStatus " +
                        ") " +
                        "FROM Order o " +
                        "JOIN o.customer c " + // (o.customer는 매핑되어 있다고 가정)
                        "JOIN Delivery d ON d.orderId = o.orderId ")
        Page<ShippingListDto> findShippingList(Pageable pageable);

        @Query("SELECT o FROM Order o " +
                        "JOIN FETCH o.customer " +
                        "LEFT JOIN FETCH o.manager " +
                        "LEFT JOIN FETCH o.orderItems " +
                        "ORDER BY o.orderId DESC")
        List<Order> findAllWithFetchJoin();

        @Query("SELECT o FROM Order o " +
                        "JOIN FETCH o.customer " +
                        "LEFT JOIN FETCH o.manager " +
                        "LEFT JOIN FETCH o.orderItems " +
                        "WHERE o.orderId = :orderId")
        Optional<Order> findByOrderIdWithFetchJoin(@Param("orderId") Integer orderId);

}
