package com.pcgear.complink.pcgear.Payment.event;

import lombok.Getter;

/**
 * 결제 완료 이벤트
 * 트랜잭션 커밋 후 알림 등의 부가 작업을 처리하기 위한 이벤트
 */
@Getter
public class PaymentCompletedEvent {

    private final Integer orderId;
    private final String message;

    public PaymentCompletedEvent(Integer orderId, String message) {
        this.orderId = orderId;
        this.message = message;
    }
}
