package com.pcgear.complink.pcgear.Order.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderStatus {
        // 결제 관련 상태
        ORDER_RECEIVED("주문 접수"), // 주문 생성 후 아직 결제되지 않음 (가상계좌 발급 대기 등)
        PAYMENT_PENDING("결제 대기"), // 가상계좌 발급 등 결제를 위한 정보가 제공된 상태
        PAID("결제 완료"), // 결제 성공
        PAYMENT_FAILED("결제 실패"), // 결제 실패
        REFUND_REQUESTED("환불 요청"), // 환불 요청
        REFUND_COMPLETED("환불 완료"), // 환불 완료

        // 배송/처리 관련 상태
        PREPARING_PRODUCT("상품 준비중"), // 결제 완료 후 상품 포장/준비
        SHIPPING_PENDING("배송 대기"),
        SHIPPING("배송중"), // 상품이 배송을 시작함
        DELIVERED("배송 완료"), // 상품 배송 완료
        PURCHASE_CONFIRMED("구매 확정"), // 사용자가 구매를 확정함

        // 취소/기타 상태
        CANCELLED("주문 취소"), // 주문 전체 취소
        UNKNOWN_ERROR("알 수 없는 오류"); // 기타 예외 상황

        private final String description;

        OrderStatus(String description) {
                this.description = description;
        }

        public String getDescription() {
                return description;
        }

        @JsonCreator // JSON 역직렬화 시 code 값으로 Enum을 찾도록 설정
        public static OrderStatus fromDescription(String description) {
                for (OrderStatus status : OrderStatus.values()) {
                        if (status.getDescription().equals(description)) {
                                return status;
                        }
                }
                throw new IllegalArgumentException("Unknown OrderStatus description: " + description);
        }

        public static OrderStatus fromNameIgnoreCase(String name) {
                for (OrderStatus status : OrderStatus.values()) {
                        if (status.name().equalsIgnoreCase(name)) { // 상수명과 대소문자 무시하고 비교
                                return status;
                        }
                }
                throw new IllegalArgumentException("Unknown OrderStatus name: " + name);
        }
}
