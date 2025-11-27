package com.pcgear.complink.pcgear.Order.repository;

import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderSearchCondition;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// static import로 Q클래스들을 가져오면 코드가 깔끔해집니다.
import static com.pcgear.complink.pcgear.Order.model.QOrder.order;
import static com.pcgear.complink.pcgear.Customer.QCustomer.customer;
// Manager가 UserEntity라면 QUserEntity로, Manager라면 QManager로 변경하세요.
import static com.pcgear.complink.pcgear.User.entity.QUserEntity.userEntity;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        @Override
        public Page<OrderResponseDto> searchOrders(OrderSearchCondition condition, Pageable pageable) {

                // 1. 컨텐츠 조회 (Projections을 사용하여 DTO로 바로 조회)
                // 주의: OrderResponseDto 생성자와 필드 타입/순서가 일치해야 합니다.
                // 만약 생성자 매칭이 힘들면 @QueryProjection을 쓰거나 fields()를 써야 합니다.
                // 여기서는 기존에 만드신 "엔티티 조회 -> 메모리 변환" 방식을 추천합니다.
                // (DTO 생성자가 엔티티를 받도록 되어있기 때문)

                // [방식 A] 엔티티 조회 (추천 - DTO 생성 로직 재활용 가능)
                List<Order> orders = queryFactory
                                .selectFrom(order)
                                .leftJoin(order.customer, customer).fetchJoin() // N+1 방지
                                .leftJoin(order.manager, userEntity).fetchJoin() // N+1 방지
                                .where(
                                                dateBetween(condition.getStartDate(), condition.getEndDate()),
                                                statusIn(condition.getOrderStatus()),
                                                managerIdEq(condition.getManagerId()),
                                                keywordContains(condition.getKeyword()))
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .orderBy(order.orderId.desc())
                                .fetch();

                // 2. 카운트 쿼리 (최적화: 조인 줄이기 가능하면 줄임)
                Long total = queryFactory
                                .select(order.count())
                                .from(order)
                                .leftJoin(order.customer, customer) // 검색 조건에 customer가 들어가므로 조인 필요
                                .leftJoin(order.manager, userEntity)
                                .where(
                                                dateBetween(condition.getStartDate(), condition.getEndDate()),
                                                statusIn(condition.getOrderStatus()),
                                                managerIdEq(condition.getManagerId()),
                                                keywordContains(condition.getKeyword()))
                                .fetchOne();

                // 3. 엔티티 -> DTO 변환
                List<OrderResponseDto> content = orders.stream()
                                .map(OrderResponseDto::new)
                                .toList();

                return new PageImpl<>(content, pageable, total != null ? total : 0);
        }

        // --- 동적 쿼리 조건 메서드 (BooleanExpression) ---

        // 1. 날짜 검색 (OrderDate 기준)
        private BooleanExpression dateBetween(LocalDate start, LocalDate end) {
                if (start == null || end == null) {
                        return null;
                }
                // 시작일 00:00:00 ~ 종료일 23:59:59
                // 만약 orderDate가 Date 타입이면 .between(start, end) 사용
                // 만약 created_at(DateTime) 기준이라면 아래처럼 변환 필요
                return order.orderDate.between(start, end);
        }

        // 2. 상태 필터 (List IN)
        private BooleanExpression statusIn(OrderStatus statuses) {
                return (statuses == null) ? null : order.orderStatus.in(statuses);
        }

        // 3. 담당자 필터 (ID 일치)
        private BooleanExpression managerIdEq(String managerId) {
                return StringUtils.hasText(managerId) ? order.manager.username.eq(managerId) : null;
        }

        // 4. 통합 키워드 검색 (주문번호 OR 고객명 OR 담당자명)
        private BooleanExpression keywordContains(String keyword) {
                if (!StringUtils.hasText(keyword)) {
                        return null;
                }
                return order.orderId.stringValue().contains(keyword) // 주문번호 (숫자->문자 변환)
                                .or(customer.customerName.contains(keyword))
                                .or(userEntity.name.contains(keyword)); // 담당자 이름
        }
}
