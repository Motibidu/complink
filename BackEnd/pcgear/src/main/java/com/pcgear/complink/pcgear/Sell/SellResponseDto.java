package com.pcgear.complink.pcgear.Sell;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellResponseDto {
        private Integer sellId;
        private Integer orderId;
        private String customerName;
        private String managerName;
        private BigDecimal totalAmount;
        private BigDecimal vatAmount;
        private BigDecimal grandAmount;
        private LocalDateTime sellDate;

        public static SellResponseDto from(Sell sell) {
                return SellResponseDto.builder()
                                .sellId(sell.getSellId())
                                .orderId(sell.getOrder() != null ? sell.getOrder().getOrderId() : null)
                                .customerName(sell.getCustomer() != null ? sell.getCustomer().getCustomerName() : null)
                                .managerName(sell.getManager() != null ? sell.getManager().getManagerName() : null)
                                .totalAmount(sell.getTotalAmount())
                                .vatAmount(sell.getVatAmount())
                                .grandAmount(sell.getGrandAmount())
                                .sellDate(sell.getSellDate())
                                .build();
        }
}
