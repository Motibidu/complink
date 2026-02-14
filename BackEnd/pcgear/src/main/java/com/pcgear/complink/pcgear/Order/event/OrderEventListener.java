package com.pcgear.complink.pcgear.Order.event;

import com.pcgear.complink.pcgear.config.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 주문 관련 이벤트를 처리하는 리스너
 * 트랜잭션 커밋 후 비동기로 알림을 전송하여 주문 로직과 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final SseEmitterManager sseEmitterManager;

    /**
     * 주문 생성 이벤트 처리
     * - 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
     * - 비동기 처리로 메인 로직에 영향 없음
     * - 알림 전송 실패해도 주문 생성 트랜잭션은 안전하게 커밋됨
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            sseEmitterManager.broadcast(event.getMessage());
            log.info("주문 생성 알림 전송 성공: orderId={}", event.getOrderId());

        } catch (Exception e) {
            // 알림 실패는 주문 생성 로직에 영향을 주지 않음
            log.error("주문 생성 알림 전송 실패 (주문은 정상 처리됨): orderId={}", event.getOrderId(), e);
        }
    }
}
