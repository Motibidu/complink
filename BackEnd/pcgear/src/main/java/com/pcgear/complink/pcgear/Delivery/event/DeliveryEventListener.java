package com.pcgear.complink.pcgear.Delivery.event;

import com.pcgear.complink.pcgear.config.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 배송 관련 이벤트를 처리하는 리스너
 * 트랜잭션 커밋 후 비동기로 알림을 전송하여 배송 로직과 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

    private final SseEmitterManager sseEmitterManager;

    /**
     * 배송 상태 업데이트 이벤트 처리
     * - 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
     * - 비동기 처리로 메인 로직에 영향 없음
     * - 알림 전송 실패해도 배송 상태 업데이트 트랜잭션은 안전하게 커밋됨
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeliveryStatusUpdated(DeliveryStatusUpdatedEvent event) {
        try {
            String notificationMessage = String.format(
                "주문번호: %d의 배송상태가 %s로 변경되었습니다. 배송조회에서 확인해주세요!",
                event.getOrderId(),
                event.getDeliveryStatus()
            );

            sseEmitterManager.broadcast(notificationMessage);
            log.info("배송 상태 업데이트 알림 전송 성공: orderId={}, status={}",
                event.getOrderId(), event.getDeliveryStatus());

        } catch (Exception e) {
            // 알림 실패는 배송 상태 업데이트 로직에 영향을 주지 않음
            log.error("배송 상태 업데이트 알림 전송 실패 (배송 상태는 정상 업데이트됨): orderId={}",
                event.getOrderId(), e);
        }
    }
}
