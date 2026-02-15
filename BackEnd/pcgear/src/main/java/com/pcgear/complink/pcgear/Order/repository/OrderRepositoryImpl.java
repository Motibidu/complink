package com.pcgear.complink.pcgear.Order.repository;

import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailRespDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderSearchCondition;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.pcgear.complink.pcgear.Order.model.QOrder.order;
import static com.pcgear.complink.pcgear.Order.model.QOrderItem.orderItem;
import static com.pcgear.complink.pcgear.Customer.QCustomer.customer;
import static com.pcgear.complink.pcgear.User.entity.QUserEntity.userEntity;
import static com.pcgear.complink.pcgear.Delivery.entity.QDelivery.delivery;

@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        @Override
        public Page<OrderResponseDto> searchOrders(OrderSearchCondition condition, Pageable pageable) {

                List<Order> orders = queryFactory
                                .selectFrom(order)
                                .leftJoin(order.customer, customer).fetchJoin() // N+1 방지
                                .leftJoin(order.manager, userEntity).fetchJoin() // N+1 방지
                                .where(
                                                dateBetween(condition.getStartDate(), condition.getEndDate()),
                                                statusIn(condition.getOrderStatus()),
                                                keywordContains(condition.getKeyword()))
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .orderBy(order.orderId.desc())
                                .fetch();
                log.info("orders: {}", orders);

                // 2. 카운트 쿼리 (최적화: 조인 줄이기 가능하면 줄임)
                Long total = queryFactory
                                .select(order.count())
                                .from(order)
                                .leftJoin(order.customer, customer)
                                .leftJoin(order.manager, userEntity)
                                .where(
                                                dateBetween(condition.getStartDate(), condition.getEndDate()),
                                                statusIn(condition.getOrderStatus()),
                                                keywordContains(condition.getKeyword()))
                                .fetchOne();

                // 3. 엔티티 -> DTO 변환
                List<OrderResponseDto> content = orders.stream()
                                .map(OrderResponseDto::new)
                                .toList();

                return new PageImpl<>(content, pageable, total != null ? total : 0);

        }

        @Override
        public AssemblyDetailRespDto getAssemblyDetailRespDto(Integer orderId) {
                Order result = queryFactory
                                .selectFrom(order)
                                .join(order.customer, customer).fetchJoin()
                                .leftJoin(order.orderItems).fetchJoin()
                                .where(order.orderId.eq(orderId))
                                .distinct()
                                .fetchOne();

                return Optional.ofNullable(result)
                                .map(o -> new AssemblyDetailRespDto(
                                                o.getOrderId(),
                                                o.getOrderStatus(),
                                                o.getAssemblyStatus(),
                                                o.getCustomer(),
                                                o.getOrderItems()))
                                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));
        }

        // 1. 날짜 검색 (OrderDate 기준)
        private BooleanExpression dateBetween(LocalDate start, LocalDate end) {
                if (start == null || end == null) {
                        return null;
                }
                return order.orderDate.between(start, end);
        }

        // 2. 상태 필터 (List IN)
        private BooleanExpression statusIn(OrderStatus status) {
                return (status == null) ? null : order.orderStatus.in(status);
        }

        // 3. 통합 키워드 검색 (주문번호 OR 고객명 OR 담당자명)
        private BooleanExpression keywordContains(String keyword) {
                if (!StringUtils.hasText(keyword)) {
                        return null;
                }
                return order.orderId.stringValue().contains(keyword) // 주문번호 (숫자->문자 변환)
                                .or(customer.customerName.contains(keyword))
                                .or(userEntity.name.contains(keyword)); // 담당자 이름
        }

        // ===== QueryDSL로 이전된 메서드들 =====

        @Override
        public Integer getTodayNewOrdersCount(LocalDateTime startOfDay, LocalDateTime endOfDay) {
                Long count = queryFactory
                                .select(order.count())
                                .from(order)
                                .where(
                                                order.createdAt.between(startOfDay, endOfDay),
                                                order.orderStatus.eq(OrderStatus.ORDER_RECEIVED))
                                .fetchOne();

                return count != null ? count.intValue() : 0;
        }

        @Override
        public Optional<Order> findByIdWithItemsAndCustomer(Integer orderId) {
                Order result = queryFactory
                                .selectFrom(order)
                                .join(order.orderItems, orderItem).fetchJoin()
                                .join(order.customer, customer).fetchJoin()
                                .where(order.orderId.eq(orderId))
                                .distinct()
                                .fetchOne();

                return Optional.ofNullable(result);
        }

        @Override
        public Page<AssemblyQueueRespDto> findAssemblyQueue(List<OrderStatus> orderStatuses, Pageable pageable) {
                List<AssemblyQueueRespDto> content = queryFactory
                                .select(Projections.constructor(AssemblyQueueRespDto.class,
                                                order.orderId,
                                                customer.customerName,
                                                userEntity.name,
                                                order.orderStatus,
                                                order.assemblyStatus,
                                                order.paidAt))
                                .from(order)
                                .join(order.customer, customer)
                                .join(order.manager, userEntity)
                                .where(order.orderStatus.in(orderStatuses))
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                Long total = queryFactory
                                .select(order.count())
                                .from(order)
                                .where(order.orderStatus.in(orderStatuses))
                                .fetchOne();

                return new PageImpl<>(content, pageable, total != null ? total : 0);
        }

        @Override
        public Page<ShippingListDto> findShippingList(Pageable pageable) {
                List<ShippingListDto> content = queryFactory
                                .select(Projections.constructor(ShippingListDto.class,
                                                order.orderId,
                                                customer.customerName,
                                                delivery.createdAt,
                                                delivery.trackingNumber,
                                                delivery.carrierId,
                                                delivery.deliveryStatus))
                                .from(order)
                                .join(order.customer, customer)
                                .join(delivery).on(delivery.orderId.eq(order.orderId))
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                Long total = queryFactory
                                .select(order.count())
                                .from(order)
                                .join(delivery).on(delivery.orderId.eq(order.orderId))
                                .fetchOne();

                return new PageImpl<>(content, pageable, total != null ? total : 0);
        }

        @Override
        public List<Order> findAllWithFetchJoin() {
                return queryFactory
                                .selectFrom(order)
                                .join(order.customer, customer).fetchJoin()
                                .leftJoin(order.manager, userEntity).fetchJoin()
                                .leftJoin(order.orderItems, orderItem).fetchJoin()
                                .orderBy(order.orderId.desc())
                                .distinct()
                                .fetch();
        }

        @Override
        public Optional<Order> findByOrderIdWithFetchJoin(Integer orderId) {
                Order result = queryFactory
                                .selectFrom(order)
                                .join(order.customer, customer).fetchJoin()
                                .leftJoin(order.manager, userEntity).fetchJoin()
                                .leftJoin(order.orderItems, orderItem).fetchJoin()
                                .where(order.orderId.eq(orderId))
                                .distinct()
                                .fetchOne();

                return Optional.ofNullable(result);
        }
}
