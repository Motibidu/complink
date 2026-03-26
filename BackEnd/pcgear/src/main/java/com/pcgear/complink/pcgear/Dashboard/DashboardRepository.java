package com.pcgear.complink.pcgear.Dashboard;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard 통계 조회를 위한 Repository 인터페이스
 */
public interface DashboardRepository {

    /**
     * 최근 7일간 일별 매출 통계 조회
     * 
     * @return 날짜별 주문 건수와 매출액 목록
     */
    List<DailySalesDto> findLast7DatesSales();

    List<DateTimesDto> findLast7DateTimesSales();

    /**
     * 카테고리별 재고 현황 조회
     * 
     * @return 카테고리별 재고 통계 목록
     */
    List<CategoryStockDto> findCategoryStockSummary();

    /**
     * 고객별 주문 통계 TOP 5 조회
     * 
     * @return 구매액 기준 상위 5명 고객 통계
     */
    List<TopCustomerDto> findTopCustomers(int limit);

    /**
     * 품목별 판매 현황 TOP 10 조회
     * 
     * @return 판매량 기준 상위 품목 통계
     */
    List<TopItemSalesDto> findTopItemSales(int limit);
}
