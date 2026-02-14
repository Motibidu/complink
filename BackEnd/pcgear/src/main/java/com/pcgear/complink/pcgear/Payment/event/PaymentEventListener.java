package com.pcgear.complink.pcgear.Payment.event;

import com.pcgear.complink.pcgear.config.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 관련 이벤트를 처리하는 리스너
 * 트랜잭션 커밋 후 비동기로 알림을 전송하여 결제 로직과 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final SseEmitterManager sseEmitterManager;

    /**
     * 결제 완료 이벤트 처리
     * - 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
     * - 비동기 처리로 메인 로직에 영향 없음
     * - 알림 전송 실패해도 결제 트랜잭션은 안전하게 커밋됨
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            String notificationMessage = String.format(
                "주문번호: %d번의 %s 판매조회에서 확인해주세요.",
                event.getOrderId(),
                event.getMessage()
            );

            sseEmitterManager.broadcast(notificationMessage);
            log.info("결제 완료 알림 전송 성공: orderId={}", event.getOrderId());

        } catch (Exception e) {
            // 알림 실패는 결제 로직에 영향을 주지 않음
            log.error("결제 완료 알림 전송 실패 (결제는 정상 처리됨): orderId={}", event.getOrderId(), e);
        }
    }
}
