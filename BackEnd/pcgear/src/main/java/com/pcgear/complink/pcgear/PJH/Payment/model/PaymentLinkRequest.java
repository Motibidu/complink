package com.pcgear.complink.pcgear.PJH.Payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 포트원 결제 URL 생성 API의 최종 요청 본문 DTO (필수 필드 위주)
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentLinkRequest {

        @JsonProperty("payment_info")
        private String paymentInfo; // JSON 문자열

        private Long expired_at; // Unix timestamp (초 단위)

        /**
         * payment_info 필드에 JSON 문자열로 들어갈 실제 결제 정보 DTO (필수 필드 위주)
         */
        @Getter
        @Setter
        @Builder
        @ToString
        public static class PaymentInfo {
                // --- 필수 필드 ---
                private String title; // 브릿지 페이지 노출문구
                private String user_code; // 고객사 식별코드
                private Integer amount; // 결제금액
                private String merchant_uid; // 주문번호
                private String name; // 제품명
                private String buyer_tel; // 주문자 연락처
                private String currency; // 통화구분코드
                private String notice_url;

                // --- 결제 수단을 위해 필요한 필드 (pay_methods 또는 direct 중 하나는 포함 권장) ---
                private List<PayMethod> pay_methods;

                /**
                 * pay_methods 배열 안의 개별 결제 수단 DTO
                 */
                @Getter
                @Setter
                @Builder
                @ToString
                public static class PayMethod {
                        private String pg;
                        private String pay_method;
                        private String label;
                }
        }
}