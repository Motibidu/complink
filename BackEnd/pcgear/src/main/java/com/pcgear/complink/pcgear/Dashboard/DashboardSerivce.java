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
public class DashboardSerivce {
        private final OrderRepository orderRepository;
        private final SellRepository sellRepository;
        private final DashboardRepository dashboardRepository;

        @Autowired
        private EntityManager entityManager;

        /**
         * 오늘의 대시보드 종합 통계 조회
         * 
         * @return 기본 통계 + 복잡한 통계 (캐싱 대상)
         */
        @Cacheable(value = "dashboard-summary")
        public TodaySummary getTodaySummary() {

                entityManager.clear();

                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                // === 기본 통계 ===
                Integer totalSales = sellRepository.getTodayTotalSales(startOfDay, endOfDay);
                Integer newOrdersCount = orderRepository.getTodayNewOrdersCount(startOfDay, endOfDay);
                Integer paymentPendingOrdersCount = orderRepository.countByOrderStatus(OrderStatus.PAYMENT_PENDING);

                List<OrderStatus> activeStatuses = List.of(
                                OrderStatus.PAID,
                                OrderStatus.PREPARING_PRODUCT,
                                OrderStatus.SHIPPING_PENDING,
                                OrderStatus.SHIPPING);
                Integer activeWorkloadCount = orderRepository.countByOrderStatusIn(activeStatuses);

                // === 복잡한 통계 (캐싱 효과 측정용) ===
                List<DailySalesDto> last7DaysSales = dashboardRepository.findLast7DaysSales();
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
