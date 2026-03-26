package com.pcgear.complink.pcgear.Dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Sell.SellRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dashboard 비즈니스 로직을 처리하는 Service
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardService {
        private final OrderRepository orderRepository;
        private final SellRepository sellRepository;
        private final DashboardRepository dashboardRepository;
        private final TopItemSalesCacheRepository topItemSalesCacheRepository;

        @Autowired
        private EntityManager entityManager;

        /**
         * 오늘의 대시보드 종합 통계 조회
         *
         * @return 기본 통계 + 복잡한 통계 (5분 캐싱)
         */
        // @Cacheable(value = "dashboard-summary", key = "'summary'")
        public TodaySummary getTodaySummary() {
                long startTime;
                long endTime;

                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                LocalDate startDate = LocalDate.now();
                LocalDate endDate = LocalDate.now();

                // === 기본 통계 ===
                startTime = System.currentTimeMillis();
                Integer totalSales = sellRepository.getTodayTotalSales(startDate, endDate);
                endTime = System.currentTimeMillis();
                log.info("totalSales: {}ms", (endTime - startTime));

                Integer newOrdersCount = orderRepository.getTodayNewOrdersCount(startOfDay, endOfDay);

                Integer paymentPendingOrdersCount = orderRepository.countByOrderStatus(OrderStatus.PAYMENT_PENDING);

                List<OrderStatus> activeStatuses = List.of(
                                OrderStatus.PAID,
                                OrderStatus.PREPARING_PRODUCT,
                                OrderStatus.SHIPPING_PENDING,
                                OrderStatus.SHIPPING);
                Integer activeWorkloadCount = orderRepository.countByOrderStatusIn(activeStatuses);

                // === 복잡한 통계 ===
                startTime = System.currentTimeMillis();
                List<DailySalesDto> last7DaysSales = dashboardRepository.findLast7DatesSales();
                endTime = System.currentTimeMillis();
                log.info("last7DaysSales: {}m", (endTime - startTime));
                List<CategoryStockDto> categoryStockSummary = dashboardRepository.findCategoryStockSummary();
                startTime = System.currentTimeMillis();
                List<TopCustomerDto> topCustomers = dashboardRepository.findTopCustomers(5);
                endTime = System.currentTimeMillis();
                log.info("topCustomers: {}m", (endTime - startTime));

                // 성능 측정 - 캐시에서 조회
                List<TopItemSalesDto> topItemSales = topItemSalesCacheRepository.findTop10ByOrderByRankPosition()
                                .stream()
                                .map(TopItemSalesCache::toDto)
                                .toList();

                return TodaySummary.builder()
                                .totalSellsToday(totalSales)
                                .newOrdersToday(newOrdersCount)
                                .pendingPaymentCount(paymentPendingOrdersCount)
                                .activeWorkloadCount(activeWorkloadCount)
                                .last7DaysSales(last7DaysSales)
                                .categoryStockSummary(categoryStockSummary)
                                .topCustomers(topCustomers)
                                .topItemSales(topItemSales)
                                .build();
        }

        //@Cacheable(value = "dashboard-summary")
        public TodaySummary getTodaySummary2() {

                entityManager.clear();

                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                LocalDate startDate = LocalDate.now();
                LocalDate endDate = LocalDate.now();

                // === 기본 통계 ===
                Integer totalSales = sellRepository.getTodayTotalSales(startDate, endDate);
                Integer newOrdersCount = orderRepository.getTodayNewOrdersCount(startOfDay, endOfDay);
                Integer paymentPendingOrdersCount = orderRepository.countByOrderStatus(OrderStatus.PAYMENT_PENDING);

                List<OrderStatus> activeStatuses = List.of(
                                OrderStatus.PAID,
                                OrderStatus.PREPARING_PRODUCT,
                                OrderStatus.SHIPPING_PENDING,
                                OrderStatus.SHIPPING);
                Integer activeWorkloadCount = orderRepository.countByOrderStatusIn(activeStatuses);

                // === 복잡한 통계 (캐싱 효과 측정용) ===
                List<DailySalesDto> last7DaysSales = dashboardRepository.findLast7DatesSales();
                List<CategoryStockDto> categoryStockSummary = dashboardRepository.findCategoryStockSummary();
                List<TopCustomerDto> topCustomers = dashboardRepository.findTopCustomers(5);
                List<TopItemSalesDto> topItemSales = dashboardRepository.findTopItemSales(10);

                return TodaySummary.builder()
                                .totalSellsToday(totalSales)
                                .newOrdersToday(newOrdersCount)
                                .pendingPaymentCount(paymentPendingOrdersCount)
                                .activeWorkloadCount(activeWorkloadCount)
                                .last7DaysSales(last7DaysSales)
                                .categoryStockSummary(categoryStockSummary)
                                .topCustomers(topCustomers)
                                .topItemSales(topItemSales)
                                .build();
        }
}
