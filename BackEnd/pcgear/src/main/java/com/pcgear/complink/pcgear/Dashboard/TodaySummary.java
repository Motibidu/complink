package com.pcgear.complink.pcgear.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodaySummary {
        // 기본 통계
        private Integer totalSellsToday;
        private Integer newOrdersToday;
        private Integer pendingPaymentCount;
        private Integer activeWorkloadCount;

        // 복잡한 통계 (캐싱 효과 측정용)
        private List<DailySalesDto> last7DaysSales;           // 최근 7일 일별 매출
        private List<CategoryStockDto> categoryStockSummary;   // 카테고리별 재고 현황
        private List<TopCustomerDto> topCustomers;             // 고객별 주문 통계 TOP 5
        private List<TopItemSalesDto> topItemSales;            // 품목별 판매 현황 TOP 10

}
