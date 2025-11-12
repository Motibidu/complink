package com.pcgear.complink.pcgear.Dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Sell.SellRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardSerivce {
        private final OrderRepository orderRepository;
        private final SellRepository sellRepository;

        @Cacheable("dashboard-summary")
        public TodaySummary getTodaySummary() {

                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                Integer totalSales = sellRepository.countBySellDateBetween(startOfDay, endOfDay);
                Integer newOrders = orderRepository.getNewOrdersToday(startOfDay, endOfDay);

                Integer pendingPayments = orderRepository.countByOrderStatus(OrderStatus.PAYMENT_PENDING);
                log.info("pendingPayments: {}", pendingPayments);

                List<OrderStatus> activeStatuses = List.of(OrderStatus.PAID, OrderStatus.PREPARING_PRODUCT,
                                OrderStatus.SHIPPING_PENDING,
                                OrderStatus.SHIPPING);
                Integer activeWorkload = orderRepository.countByOrderStatusIn(activeStatuses);

                // 4. 하나의 객체로 만들어 반환
                return TodaySummary.builder()
                                .totalSellsToday(totalSales)
                                .newOrdersToday(newOrders)
                                .pendingPaymentCount(pendingPayments)
                                .activeWorkloadCount(activeWorkload)
                                .build();
        }
}
