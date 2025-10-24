package com.pcgear.complink.pcgear.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    boolean existsByCustomerCustomerId(String customerId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    Optional<Order> findByMerchantUid(String merchantUid);

    List<Order> findAllByOrderStatus(OrderStatus orderStatus);

    List<Order> findAllByOrderStatusIn(List<OrderStatus> orderStatuses);

    // @Param 어노테이션을 사용하여 :orderId와 매개변수 orderId를 명시적으로 연결해야 합니다.
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH o.customer c " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithItemsAndCustomer(@Param("orderId") Integer orderId);

}
