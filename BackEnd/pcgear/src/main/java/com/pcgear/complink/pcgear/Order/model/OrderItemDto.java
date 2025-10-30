package com.pcgear.complink.pcgear.Order.model;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class OrderItemDto {

        private final Integer orderItemId;
        private final String itemName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal totalPrice;

        // OrderItem 엔티티를 DTO로 변환하는 생성자
        public OrderItemDto(OrderItem orderItem) {
                this.orderItemId = orderItem.getOrderItemId();
                this.itemName = orderItem.getItemName();
                this.quantity = orderItem.getQuantity();
                this.unitPrice = orderItem.getUnitPrice();
                this.totalPrice = orderItem.getTotalPrice();
        }
}
