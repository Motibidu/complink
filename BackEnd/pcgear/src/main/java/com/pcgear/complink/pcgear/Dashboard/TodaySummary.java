package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodaySummary {
        private Integer totalSellsToday;
        private Integer newOrdersToday;
        private Integer pendingPaymentCount;
        private Integer activeWorkloadCount;

}
