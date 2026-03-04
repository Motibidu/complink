package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDto {
    private String customerId;
    private String customerName;
    private Long orderCount;
    private Long totalPurchase;
    private Long avgOrderValue;
}
