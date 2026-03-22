package com.pcgear.complink.pcgear.Delivery.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 웹훅 등록 이벤트
 * - 배송 추적 웹훅 등록 요청 시 발행
 */
@Getter
@AllArgsConstructor
public class DeliveryWebhookRegisterEvent {
        private final String accessToken;
        private final String carrierId;
        private final String trackingNumber;
        private final String callbackUrl;
        private final Long deliveryId;
        private final Integer orderId;
}
