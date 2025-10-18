package com.pcgear.complink.pcgear.Payment.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 포트원 결제 URL 생성 API의 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentLinkResponse {
        private String shortenedUrl;
}
