package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesDto {
    private LocalDate date;
    private Long orderCount;
    private Long totalSales;
}
