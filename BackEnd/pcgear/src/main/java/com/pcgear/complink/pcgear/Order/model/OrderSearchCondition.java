package com.pcgear.complink.pcgear.Order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderSearchCondition {

        // 1. 기간 검색 (주문일 기준)
        // @DateTimeFormat: 프론트에서 "yyyy-MM-dd" 문자열로 보내도 LocalDate로 자동 변환해줌
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        // 2. 상태 필터 (멀티 체크 가능)
        // 예: ?orderStatuses=PAID,SHIPPING (콤마로 구분되어 들어오면 리스트로 자동 변환됨)
        private OrderStatus orderStatus;

        // 3. 통합 검색어 (주문번호, 고객명, 연락처 등)
        private String keyword;

        // 4. 담당자 필터 (특정 직원의 주문만 보기)
        private String managerId;

        // 5. (선택) 고객사 ID 필터 (특정 고객사의 주문만 보기)
        private String customerId;
}
