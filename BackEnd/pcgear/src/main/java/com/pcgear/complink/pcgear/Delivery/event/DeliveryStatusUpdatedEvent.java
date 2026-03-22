package com.pcgear.complink.pcgear.Delivery.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 배송 상태 업데이트 이벤트
 * 트랜잭션 커밋 후 알림 등의 부가 작업을 처리하기 위한 이벤트
 */
@Getter
@AllArgsConstructor
public class DeliveryStatusUpdatedEvent {

    private final Integer orderId;
    private final String deliveryStatus;
}
