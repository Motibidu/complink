package com.pcgear.complink.pcgear.PJH.Order.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.pcgear.complink.pcgear.PJH.Customer.CustomerDto;
import com.pcgear.complink.pcgear.PJH.Manager.ManagerDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private final ManagerDto manager;
    private final List<OrderItemDto> items;

    public OrderResponseDto(Order order) {
        this.orderId = order.getOrderId();
        this.orderDate = order.getOrderDate();
        this.deliveryDate = order.getDeliveryDate();
        this.orderStatusDesc = order.getOrderStatus().getDescription();
        this.totalAmount = order.getTotalAmount();
        this.vatAmount = order.getVatAmount();
        this.grandAmount = order.getGrandAmount();
        this.paymentLink = order.getPaymentLink();

        this.customer = new CustomerDto(order.getCustomer());
        if (order.getManager() != null) {
            // 2. null이 아닐 때만 ManagerDto를 생성합니다.
            this.manager = new ManagerDto(order.getManager());
        } else {
            // 3. null일 경우, manager 필드도 null로 설정합니다.
            this.manager = null;
        }
        this.items = order.getItems().stream()
                .map(OrderItemDto::new) // .map(orderItem -> new OrderItemDto(orderItem))과 동일
                .collect(Collectors.toList());
    }
}
