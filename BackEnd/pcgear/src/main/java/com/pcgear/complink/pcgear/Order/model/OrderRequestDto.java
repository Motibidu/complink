package com.pcgear.complink.pcgear.Order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.pcgear.complink.pcgear.Item.ItemCategory;

@Getter
@Setter
@ToString
public class OrderRequestDto {
    private LocalDate orderDate;

    private String customerId;
    private String customerName;
    private String managerId;
    private String managerName;
    private LocalDate deliveryDate;
    private String status; // 주문 상태 필드 추가
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private BigDecimal vatAmount; // vat -> vatAmount로 필드명 변경
    private BigDecimal grandAmount;

    @Getter
    @Setter
    @ToString
    public static class OrderItemDto {
        private Integer itemId;
        private ItemCategory itemCategory;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
