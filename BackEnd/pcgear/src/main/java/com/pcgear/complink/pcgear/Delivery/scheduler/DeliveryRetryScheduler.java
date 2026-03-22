package com.pcgear.complink.pcgear.Delivery.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pcgear.complink.pcgear.Delivery.DeliveryRepository;
import com.pcgear.complink.pcgear.Delivery.DeliveryService;
import com.pcgear.complink.pcgear.Delivery.DeliveryStateManager;
import com.pcgear.complink.pcgear.Delivery.DeliveryWebhookResisgterRepository;
import com.pcgear.complink.pcgear.Delivery.WebhookService;
import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.entity.DeliveryWebhookRegisterRetry;
import com.pcgear.complink.pcgear.Delivery.enums.RetryStatus;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹훅 등록 재시도 스케줄러
 * - 1분마다 실행
 * - ACTIVE 상태의 재시도 대상을 조회하여 웹훅 재등록 시도
 *
 * 책임: 재시도 대상 조회 및 웹훅 재등록 조정 (실제 작업은 WebhookService와 StateManager에 위임)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryRetryScheduler {

        private final DeliveryWebhookResisgterRepository retryRepository;
        private final DeliveryRepository deliveryRepository;
        private final DeliveryService deliveryService;
        private final WebhookService webhookService;
        private final DeliveryStateManager stateManager;

        private static final int MAX_RETRY_COUNT = 5;

        /**
         * 재시도 대상 처리
         * - 1분마다 실행
         * - nextRunAt이 현재 시간 이전인 ACTIVE 상태 조회
         */
        @Scheduled(fixedDelay = 60000)
        @Transactional
        public void processPendingRetries() {
                List<DeliveryWebhookRegisterRetry> targets = retryRepository
                        .findAllByStatusAndNextRunAtBefore(RetryStatus.ACTIVE, LocalDateTime.now());

                log.info("재시도 대상 {}건 처리 시작", targets.size());

                for (DeliveryWebhookRegisterRetry retry : targets) {
                        processRetry(retry);
                }
        }

        /**
         * 개별 재시도 처리
         */
        private void processRetry(DeliveryWebhookRegisterRetry retry) {
                try {
                        Delivery delivery = deliveryRepository.findById(retry.getTargetId())
                                .orElseThrow(() -> new EntityNotFoundException("배송 정보 없음: " + retry.getTargetId()));

                        log.info("웹훅 재등록 시도: DeliveryId={}, RetryCount={}",
                                delivery.getDeliveryId(), retry.getRetryCount());

                        // AccessToken 발급
                        String accessToken = deliveryService.getAccessToken();

                        // 웹훅 재등록 (WebhookService가 재시도 포함)
                        webhookService.registerWebhook(
                                accessToken,
                                delivery.getCarrierId(),
                                delivery.getTrackingNumber(),
                                delivery.getCarrierId() + "/webhook");

                        // 성공 시 상태 업데이트
                        stateManager.markRetryAsSuccess(retry);
                } catch (Exception e) {
                        log.error("웹훅 재등록 실패: RetryId={}, DeliveryId={}, Error={}",
                                retry.getId(), retry.getTargetId(), e.getMessage());

                        // 재시도 횟수 확인
                        if (retry.getRetryCount() >= MAX_RETRY_COUNT) {
                                // 최종 실패
                                stateManager.markRetryAsExhausted(retry);
                        } else {
                                // 재시도 횟수 증가 및 다음 실행 시간 업데이트
                                stateManager.incrementRetry(retry, e.getMessage());
                        }
                }
        }
}
