package com.pcgear.complink.pcgear.Order.event;

import lombok.Getter;

/**
 * 주문 생성 이벤트
 * 트랜잭션 커밋 후 알림 등의 부가 작업을 처리하기 위한 이벤트
 */
@Getter
public class OrderCreatedEvent {

    private final Integer orderId;
    private final String message;

    public OrderCreatedEvent(Integer orderId, String message) {
        this.orderId = orderId;
        this.message = message;
    }
}
