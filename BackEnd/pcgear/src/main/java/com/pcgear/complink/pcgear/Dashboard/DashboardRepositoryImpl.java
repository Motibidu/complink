package com.pcgear.complink.pcgear.Dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Item.ItemCategory;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.pcgear.complink.pcgear.Order.model.QOrder.order;
import static com.pcgear.complink.pcgear.Order.model.QOrderItem.orderItem;
import static com.pcgear.complink.pcgear.Item.QItem.item;
import static com.pcgear.complink.pcgear.Customer.QCustomer.customer;

/**
 * Dashboard 통계 조회를 위한 Repository 구현체
 * QueryDSL을 사용하여 복잡한 통계 쿼리를 수행
 */
@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {

        private final JPAQueryFactory queryFactory;

        /**
         * 최근 7일간 일별 매출 통계 조회
         * - 복잡한 쿼리: JOIN + GROUP BY + 날짜 범위 필터링
         */
        @Override
        public List<DailySalesDto> findLast7DaysSales() {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(6);

                List<Tuple> results = queryFactory
                                .select(
                                                order.orderDate,
                                                order.orderId.count(),
                                                order.grandAmount.sum())
                                .from(order)
                                .where(
                                                order.orderDate.between(startDate, endDate),
                                                order.orderStatus.ne(OrderStatus.CANCELLED))
                                .groupBy(order.orderDate)
                                .orderBy(order.orderDate.asc())
                                .fetch();

                return results.stream()
                                .map(row -> new DailySalesDto(
                                                row.get(0, LocalDate.class),
                                                row.get(1, Long.class),
                                                row.get(2, BigDecimal.class).longValue()))
                                .collect(Collectors.toList());
        }

        /**
         * 카테고리별 재고 현황 조회
         * - 복잡한 쿼리: 집계 + 계산 + GROUP BY
         */
        @Override
        public List<CategoryStockDto> findCategoryStockSummary() {
                List<Tuple> results = queryFactory
                                .select(
                                                item.itemCategory,
                                                item.itemId.count(),
                                                item.QuantityOnHand.sum(),
                                                item.AvailableQuantity.sum(),
                                                item.AvailableQuantity.multiply(item.sellingPrice).sum())
                                .from(item)
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
                List<Tuple> results = queryFactory
                                .select(
                                                customer.customerId,
                                                customer.customerName,
                                                order.orderId.count(),
                                                order.grandAmount.sum(),
                                                order.grandAmount.avg())
                                .from(order)
                                .join(order.customer, customer)
                                .where(order.orderStatus.ne(OrderStatus.CANCELLED))
                                .groupBy(customer.customerId, customer.customerName)
                                .orderBy(order.grandAmount.sum().desc())
                                .limit(limit)
                                .fetch();

                return results.stream()
                                .map(row -> new TopCustomerDto(
                                                row.get(0, String.class),
                                                row.get(1, String.class),
                                                row.get(2, Long.class),
                                                row.get(3, BigDecimal.class).longValue(),
                                                row.get(4, Double.class).longValue()))
                                .collect(Collectors.toList());
        }

        /**
         * 품목별 판매 현황 TOP N 조회
         * - 복잡한 쿼리: JOIN + GROUP BY + 집계 + ORDER BY + LIMIT
         */
        @Override
        public List<TopItemSalesDto> findTopItemSales(int limit) {
                List<Tuple> results = queryFactory
                                .select(
                                                item.itemId,
                                                item.itemName,
                                                orderItem.quantity.sum(),
                                                orderItem.quantity.multiply(item.sellingPrice).sum(),
                                                item.AvailableQuantity)
                                .from(orderItem)
                                .join(orderItem.item, item)
                                .join(orderItem.order, order)
                                .where(order.orderStatus.ne(OrderStatus.CANCELLED))
                                .groupBy(item.itemId, item.itemName, item.AvailableQuantity)
                                .orderBy(orderItem.quantity.sum().desc())
                                .limit(limit)
                                .fetch();

                List<TopItemSalesDto> dtoList = new ArrayList<>();
                for (Tuple row : results) {
                        Integer itemId = row.get(0, Integer.class);
                        String itemName = row.get(1, String.class);
                        Integer quantitySum = row.get(2, Integer.class);
                        Integer revenueSum = row.get(3, Integer.class);
                        Integer availableQty = row.get(4, Integer.class);

                        dtoList.add(new TopItemSalesDto(
                                        itemId,
                                        itemName,
                                        quantitySum != null ? quantitySum.longValue() : 0L,
                                        revenueSum != null ? revenueSum.longValue() : 0L,
                                        availableQty != null ? availableQty.longValue() : 0L));
                }
                return dtoList;
        }
}
