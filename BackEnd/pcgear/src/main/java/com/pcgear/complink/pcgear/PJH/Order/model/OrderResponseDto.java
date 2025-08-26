package com.pcgear.complink.pcgear.PJH.Order.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public class OrderResponseDto {
        private final Integer orderId;
        private final LocalDate orderDate;
        private final LocalDate deliveryDate;
        private final String status;
        private final BigDecimal totalAmount;
        private final BigDecimal vatAmount;
        private final BigDecimal grandAmount;

        private final CustomerDto customer;
        private final UserDto manager;
        private final List<OrderItemDto> items;

        public OrderResponseDto(Order order) {
                this.orderId = order.getOrderId();
                this.orderDate = order.getOrderDate();
                this.deliveryDate = order.getDeliveryDate();
                this.status = order.getStatus();
                this.totalAmount = order.getTotalAmount();
                this.vatAmount = order.getVatAmount();
                this.grandAmount = order.getGrandAmount();

                this.customer = new CustomerDto(order.getCustomer());
                this.manager = new UserDto(order.getManager());
                this.items = order.getItems().stream()
                                .map(OrderItemDto::new) // .map(orderItem -> new OrderItemDto(orderItem))과 동일
                                .collect(Collectors.toList());
        }
}
