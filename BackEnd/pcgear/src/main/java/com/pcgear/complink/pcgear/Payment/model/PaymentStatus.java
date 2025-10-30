package com.pcgear.complink.pcgear.Payment.model;

public enum PaymentStatus {

        /**
         * * 결제 요청은 되었으나, 아직 최종 승인/거절 상태가 아님.
         * 카드 정보 입력 대기, 가상계좌 발급 대기 등 초기 상태에 해당함.
         */
        READY("결제 준비"),
        PAID("결제 완료"),
        CANCELLED("결제 취소/환불"),
        FAILED("결제 실패"),
        SCHEDULED("결제 예약"),
        UNKNOWN("알 수 없는 상태");

        private final String description;

        PaymentStatus(String description) {
                this.description = description;
        }

        public String getDescription() {
                return description;
        }

        /**
         * PortOne API 응답 코드(String)를 해당 Enum 값으로 변환하는 정적 팩토리 메서드입니다.
         */
        public static PaymentStatus fromNameIgnoreCase(String name) {
                if (name == null) {
                        return UNKNOWN;
                }
                for (PaymentStatus status : PaymentStatus.values()) {
                        if (status.name().equalsIgnoreCase(name.trim())) {
                                return status;
                        }
                }
                return UNKNOWN;
        }
}
