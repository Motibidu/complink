package com.pcgear.complink.pcgear.Delivery.event;

import com.pcgear.complink.pcgear.Delivery.DeliveryStateManager;
import com.pcgear.complink.pcgear.Delivery.WebhookService;
import com.pcgear.complink.pcgear.Delivery.exception.WebhookRegistrationException;
import com.pcgear.complink.pcgear.config.SseEmitterManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 배송 관련 이벤트를 처리하는 리스너
 * - 트랜잭션 커밋 후 비동기로 처리
 * - 배송 상태 알림 전송
 * - 웹훅 등록 및 상태 업데이트
 *
 * 책임: 이벤트 수신 및 적절한 서비스로 위임
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

        private final SseEmitterManager sseEmitterManager;
        private final WebhookService webhookService;
        private final DeliveryStateManager stateManager;

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
                                        event.getDeliveryStatus());

                        sseEmitterManager.broadcast(notificationMessage);
                        log.info("배송 상태 업데이트 알림 전송 성공: orderId={}, status={}",
                                        event.getOrderId(), event.getDeliveryStatus());

                } catch (Exception e) {
                        // 알림 실패는 배송 상태 업데이트 로직에 영향을 주지 않음
                        log.error("배송 상태 업데이트 알림 전송 실패 (배송 상태는 정상 업데이트됨): orderId={}",
                                        event.getOrderId(), e);
                }
        }

        /**
         * 웹훅 등록 이벤트 처리
         * - 웹훅 등록 시도 (WebhookService에 위임)
         * - 성공 시: 상태 업데이트 (DeliveryStateManager에 위임)
         * - 실패 시: 재시도 등록 (DeliveryStateManager에 위임)
         */
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handleWebhookRegister(DeliveryWebhookRegisterEvent event) {
                log.info("웹훅 등록 이벤트 수신: DeliveryId={}, carrierId={}, trackingNumber={}",
                        event.getDeliveryId(), event.getCarrierId(), event.getTrackingNumber());

                try {
                        // 웹훅 등록 (WebhookService가 재시도 포함)
                        webhookService.registerWebhook(
                                event.getAccessToken(),
                                event.getCarrierId(),
                                event.getTrackingNumber(),
                                event.getCallbackUrl());

                        // 성공 시 상태 업데이트
                        stateManager.markWebhookAsSuccess(event.getDeliveryId(), event.getOrderId());

                        log.info("웹훅 등록 성공: DeliveryId={}", event.getDeliveryId());

                } catch (WebhookRegistrationException e) {
                        // 실패 시 재시도 등록
                        stateManager.registerRetry(event.getDeliveryId(), e.getMessage());

                        log.warn("웹훅 등록 실패 (재시도 등록 완료): DeliveryId={}, Error={}",
                                event.getDeliveryId(), e.getMessage());
                }
        }
}
