package com.pcgear.complink.pcgear.Dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Item.ItemCategory;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import static com.pcgear.complink.pcgear.Order.model.QOrder.order;
import static com.pcgear.complink.pcgear.Sell.QSell.sell;
import static com.pcgear.complink.pcgear.Order.model.QOrderItem.orderItem;
import static com.pcgear.complink.pcgear.Item.QItem.item;
import static com.pcgear.complink.pcgear.Customer.QCustomer.customer;
import static com.querydsl.core.types.Projections.constructor;

/**
 * Dashboard 통계 조회를 위한 Repository 구현체
 * QueryDSL을 사용하여 복잡한 통계 쿼리를 수행
 */
@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {

        private final JPAQueryFactory queryFactory;
        private final EntityManager entityManager;

        @Override
        public List<DailySalesDto> findLast7DatesSales() {

                LocalDate startDate = LocalDate.now().minusDays(6);
                LocalDate endDate = LocalDate.now();

                List<DailySalesDto> results = queryFactory
                                .select(Projections.constructor(DailySalesDto.class,
                                                sell.date,
                                                sell.sellId.count(),
                                                sell.grandAmount.sum()))
                                .from(sell)
                                .where(
                                                sell.date.between(startDate, endDate))
                                .groupBy(sell.date)
                                .orderBy(sell.date.asc())
                                .fetch();
                return results;
        }

        @Override
        public List<DateTimesDto> findLast7DateTimesSales() {

                LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
                LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                List<DateTimesDto> results = queryFactory
                                .select(Projections.constructor(DateTimesDto.class,
                                                sell.dateTime,
                                                sell.sellId.count(),
                                                sell.grandAmount.sum()))
                                .from(sell)
                                .where(
                                                sell.dateTime.between(startDateTime, endDateTime))
                                .groupBy(sell.dateTime)
                                .orderBy(sell.dateTime.asc())
                                .fetch();
                return results;
        }

        /**
         * 카테고리별 재고 현황 조회
         * - 복잡한 쿼리: 집계 + 계산 + GROUP BY
         * - NULL/빈 문자열 카테고리 제외
         */
        @Override
        public List<CategoryStockDto> findCategoryStockSummary() {
                List<Tuple> results = queryFactory
                                .select(
                                                item.itemCategory,
                                                item.itemId.count(),
                                                item.quantityOnHand.sum(),
                                                item.availableQuantity.sum(),
                                                item.availableQuantity.multiply(item.sellingPrice).sum())
                                .from(item)
                                .where(item.itemCategory.isNotNull())
                                .groupBy(item.itemCategory)
                                .orderBy(item.itemCategory.asc())
                                .fetch();

                List<CategoryStockDto> dtoList = new ArrayList<>();
                for (Tuple row : results) {
                        ItemCategory category = row.get(0, ItemCategory.class);
                        Long totalItems = row.get(1, Long.class);
                        Integer totalStock = row.get(2, Integer.class);
                        Integer totalAvailable = row.get(3, Integer.class);
                        Integer totalValue = row.get(4, Integer.class);

                        dtoList.add(new CategoryStockDto(
                                        category,
                                        totalItems != null ? totalItems : 0L,
                                        totalStock != null ? totalStock.longValue() : 0L,
                                        totalAvailable != null ? totalAvailable.longValue() : 0L,
                                        totalValue != null ? totalValue : 0));
                }
                return dtoList;
        }

        /**
         * 고객별 주문 통계 TOP N 조회
         * - 복잡한 쿼리: JOIN + GROUP BY + 집계 + ORDER BY + LIMIT
         */
        @Override
        public List<TopCustomerDto> findTopCustomers(int limit) {
                List<TopCustomerDto> results = queryFactory
                                .select(Projections.constructor(TopCustomerDto.class, 
                                                customer.customerId,
                                                customer.customerName,
                                                order.orderId.count(),
                                                order.grandAmount.sum(),
                                                order.grandAmount.avg()))
                                .from(order)
                                .join(order.customer, customer)
                                .where(order.orderStatus.ne(OrderStatus.CANCELLED))
                                .groupBy(customer.customerId)
                                .orderBy(order.grandAmount.sum().desc())
                                .limit(limit)
                                .fetch();

                return results;
        }

        /**
         * 품목별 판매 현황 TOP N 조회
         * - 최적화: order_items에서 시작하여 IN 서브쿼리로 필터링
         * - 서브쿼리가 먼저 실행되어 유효한 order_id만 추출
         */
        @Override
        public List<TopItemSalesDto> findTopItemSales(int limit) {
                List<TopItemSalesDto> dtoList = queryFactory
                                .select(constructor(TopItemSalesDto.class,
                                                item.itemId,
                                                item.itemName,
                                                orderItem.quantity.sum(),
                                                orderItem.quantity.multiply(orderItem.unitPrice).sum().longValue(),
                                                item.availableQuantity))
                                .from(orderItem)
                                .join(orderItem.item, item)
                                .join(orderItem.order, order)
                                .where(order.orderStatus.in(OrderStatus.PAID, OrderStatus.PREPARING_PRODUCT,
                                                OrderStatus.SHIPPING_PENDING, OrderStatus.SHIPPING,
                                                OrderStatus.DELIVERED))
                                .groupBy(item.itemId, item.itemName, item.availableQuantity)
                                .orderBy(orderItem.quantity.sum().desc())
                                .limit(limit)
                                .fetch();

                return dtoList;
        }
}
