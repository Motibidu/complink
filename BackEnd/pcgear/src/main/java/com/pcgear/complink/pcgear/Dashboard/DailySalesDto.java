package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesDto {
    private LocalDateTime date;
    private Long salesCount;
    private BigDecimal totalSales;
}
