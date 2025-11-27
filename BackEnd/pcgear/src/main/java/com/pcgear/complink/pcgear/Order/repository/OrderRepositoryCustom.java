package com.pcgear.complink.pcgear.Order.repository;

import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
        Page<OrderResponseDto> searchOrders(OrderSearchCondition condition, Pageable pageable);
}
