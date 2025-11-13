package com.pcgear.complink.pcgear.Delivery.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;

public enum DeliveryStatus {
        UNKNOWN("알 수 없음"),

        /**
         * 상품 준비중 (API Value: INFORMATION_RECEIVED)
         */
        INFORMATION_RECEIVED("상품준비중"),

        /**
         * 집화 완료 (API Value: AT_PICKUP)
         */
        AT_PICKUP("집화처리"),

        /**
         * 배송중 (API Value: IN_TRANSIT)
         */
        IN_TRANSIT("배송중"),

        /**
         * 배송 출발 (API Value: OUT_FOR_DELIVERY)
         */
        OUT_FOR_DELIVERY("배송출발"),

        /**
         * 배달 시도 실패 (API Value: ATTEMPT_FAIL)
         */
        ATTEMPT_FAIL("배달실패"),

        /**
         * 배송 완료 (API Value: DELIVERED)
         */
        DELIVERED("배송완료"),

        /**
         * 픽업 가능 (경비실, 픽업센터 등) (API Value: AVAILABLE_FOR_PICKUP)
         */
        AVAILABLE_FOR_PICKUP("픽업가능"),

        /**
         * 배송 예외 (파손, 분실 등) (API Value: EXCEPTION)
         */
        EXCEPTION("배송예외");

        private final String description;

        DeliveryStatus(String description) {
                this.description = description;
        }

        public String getDescription() {
                return description;
        }

        @JsonCreator // JSON 역직렬화 시 code 값으로 Enum을 찾도록 설정
        public static DeliveryStatus fromDescription(String description) {
                for (DeliveryStatus status : DeliveryStatus.values()) {
                        if (status.getDescription().equals(description)) {
                                return status;
                        }
                }
                throw new IllegalArgumentException("Unknown DeliveryStatus description: " + description);
        }

        @JsonCreator
        public static DeliveryStatus fromValue(String name) {
                for (DeliveryStatus status : DeliveryStatus.values()) {
                        if (status.name().equalsIgnoreCase(name)) { // 상수명과 대소문자 무시하고 비교
                                return status;
                        }
                }
                throw new IllegalArgumentException("Unknown DeliveryStatus name: " + name);
        }

}
