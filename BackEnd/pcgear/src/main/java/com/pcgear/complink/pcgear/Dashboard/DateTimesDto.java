package com.pcgear.complink.pcgear.Dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateTimesDto {
        private LocalDateTime dateTime;
        private Long salesCount;
        private BigDecimal totalSales;

}
