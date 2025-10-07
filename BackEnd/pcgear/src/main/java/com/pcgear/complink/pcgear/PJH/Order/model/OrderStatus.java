package com.pcgear.complink.pcgear.PJH.Order.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pcgear.complink.pcgear.PJH.Payment.model.PaymentStatus;

public enum OrderStatus {
        // 결제 관련 상태
        PENDING_PAYMENT(0, "결제 대기"), // 주문 생성 후 아직 결제되지 않음 (가상계좌 발급 대기 등)
        PAYMENT_READY(1, "결제 준비"), // 가상계좌 발급 등 결제를 위한 정보가 제공된 상태
        PAID(2, "결제 완료"), // 결제 성공
        PAYMENT_FAILED(3, "결제 실패"), // 결제 실패
        REFUND_REQUESTED(4, "환불 요청"), // 환불 요청
        REFUND_COMPLETED(5, "환불 완료"), // 환불 완료

        // 배송/처리 관련 상태
        PREPARING_PRODUCT(10, "상품 준비중"), // 결제 완료 후 상품 포장/준비
        SHIPPING(11, "배송중"), // 상품이 배송을 시작함
        DELIVERED(12, "배송 완료"), // 상품 배송 완료
        PURCHASE_CONFIRMED(13, "구매 확정"), // 사용자가 구매를 확정함

        // 취소/기타 상태
        CANCELLED(20, "주문 취소"), // 주문 전체 취소
        UNKNOWN_ERROR(99, "알 수 없는 오류"); // 기타 예외 상황

        private final int code;
        private final String description;

        OrderStatus(int code, String description) {
                this.code = code;
                this.description = description;
        }

        @JsonValue // JSON 직렬화 시 code 값을 사용하도록 설정
        public int getCode() {
                return code;
        }

        public String getDescription() {
                return description;
        }

        // code 값으로 Enum을 찾기 위한 정적 팩토리 메서드
        @JsonCreator // JSON 역직렬화 시 code 값으로 Enum을 찾도록 설정
        public static OrderStatus fromCode(int code) {
                for (OrderStatus status : OrderStatus.values()) {
                        if (status.getCode() == code) {
                                return status;
                        }
                }
                // 찾지 못하면 기본값을 반환하거나 예외를 던질 수 있습니다.
                throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
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
