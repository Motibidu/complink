package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesDto {
    private LocalDate date;
    private Long salesCount;
    private BigDecimal totalSales;
}
