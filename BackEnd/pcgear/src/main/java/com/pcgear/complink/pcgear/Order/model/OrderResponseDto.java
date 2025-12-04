package com.pcgear.complink.pcgear.Order.model;

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
    private final String orderStatusDesc;
    private final String paymentLink;
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
        this.totalAmount = order.getTotalAmount();
        this.vatAmount = order.getVatAmount();
        this.grandAmount = order.getGrandAmount();
        this.paymentLink = order.getPaymentLink();

        if (order.getOrderStatus() != null) {
            this.orderStatusDesc = order.getOrderStatus().getDescription();
        } else {
            this.orderStatusDesc = "상태 정보 없음";
        }
        this.manager= new UserDto(order.getManager());
        this.customer = new CustomerDto(order.getCustomer());
        this.items = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }
}
