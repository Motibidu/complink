package com.pcgear.complink.pcgear.PJH.Order.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderRequestDto {
    private LocalDate orderDate;
    // 스키마 변경에 따라 String이 아닌 ID(Long)를 받도록 수정
    private Long customerId; 
    private Long managerId;
    private LocalDate deliveryDate;
    private String status; // 주문 상태 필드 추가
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private BigDecimal vatAmount; // vat -> vatAmount로 필드명 변경
    private BigDecimal grandTotal;

    @Getter
    @Setter
    public static class OrderItemDto {
        private String partNumber;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal itemTotal; // total -> itemTotal로 필드명 변경
    }
}
