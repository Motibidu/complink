package com.pcgear.complink.pcgear.Order.repository;

import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailRespDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderSearchCondition;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryCustom {
        Page<OrderResponseDto> searchOrders(OrderSearchCondition condition, Pageable pageable);

        AssemblyDetailRespDto getAssemblyDetailRespDto(Integer orderId);

        // QueryDSL로 이전된 메서드들
        Integer getTodayNewOrdersCount(LocalDateTime startOfDay, LocalDateTime endOfDay);

        Optional<Order> findByIdWithItemsAndCustomer(Integer orderId);

        Page<AssemblyQueueRespDto> findAssemblyQueue(List<OrderStatus> orderStatuses, Pageable pageable);

        Page<ShippingListDto> findShippingList(Pageable pageable);

        List<Order> findAllWithFetchJoin();

        Optional<Order> findByOrderIdWithFetchJoin(Integer orderId);
}
