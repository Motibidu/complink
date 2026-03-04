package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TopItemSalesDto {
    private Integer itemId;
    private String itemName;
    private Long quantitySold;
    private Long totalRevenue;
    private Long currentStock;
}
