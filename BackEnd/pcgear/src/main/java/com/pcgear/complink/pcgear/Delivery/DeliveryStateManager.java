package com.pcgear.complink.pcgear.Delivery;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.entity.DeliveryWebhookRegisterRetry;
import com.pcgear.complink.pcgear.Delivery.enums.DeliveryWebhookStatus;
import com.pcgear.complink.pcgear.Delivery.enums.RetryStatus;
import com.pcgear.complink.pcgear.Delivery.model.req.TrackingNumberReq;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 배송 상태 관리 전담 컴포넌트
 * - 배송 엔티티 생성/업데이트
 * - 웹훅 상태 변경
 * - 재시도 등록 및 관리
 *
 * 책임: 배송 및 재시도 엔티티의 상태만 관리 (비즈니스 로직 제외)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryStateManager {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryWebhookResisgterRepository retryRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ItemService itemService;

    /**
     * 배송 정보만 저장 (재고 차감 없음, webhookStatus=PENDING)
     */
    @Transactional
    public Delivery createDeliveryAsPending(TrackingNumberReq req) {
        Delivery delivery = Delivery.builder()
                .carrierId(req.getCarrierId())
                .trackingNumber(req.getTrackingNumber())
                .orderId(req.getOrderId())
                .customerId(req.getCustomerId())
                .webhookStatus(DeliveryWebhookStatus.PENDING)
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("배송 정보 저장 완료 (PENDING): DeliveryId={}", saved.getDeliveryId());
        return saved;
    }

    /**
     * 웹훅 등록 성공 시 실행
     * - webhookStatus = SUCCESS
     * - 재고 차감
     * - 주문 상태 변경 (조립 완료 -> 배송 대기)
     */
    @Transactional
    public void markWebhookAsSuccess(Long deliveryId, Integer orderId) {
        // 1. 배송 상태 업데이트
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("배송 정보 없음: " + deliveryId));
        delivery.setWebhookStatus(DeliveryWebhookStatus.SUCCESS);
        delivery.setWebhookRegisteredAt(LocalDateTime.now());
        deliveryRepository.save(delivery);


        // 2. 주문 상태 변경 (조립 완료 -> 배송 대기)
        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING_PENDING);

        // 3. 실재고 차감 (OrderItems fetch join으로 조회)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 없음: " + orderId));
        itemService.updateItemQuantityOnHand(order);


        log.info("배송 등록 완료 (재고 차감 완료): DeliveryId={}, OrderId={}", deliveryId, orderId);
    }

    /**
     * 웹훅 등록 실패 시 실행
     * - 재시도 테이블에 등록
     * - webhookStatus는 PENDING 유지 (재시도 대상)
     */
    @Transactional
    public void registerRetry(Long deliveryId, String errorMessage) {
        DeliveryWebhookRegisterRetry retry = DeliveryWebhookRegisterRetry.builder()
                .targetId(deliveryId)
                .status(RetryStatus.ACTIVE)
                .nextRunAt(LocalDateTime.now().plusMinutes(5))
                .lastError(errorMessage)
                .build();

        retryRepository.save(retry);

        log.warn("웹훅 등록 실패 기록 및 재시도 등록: DeliveryId={}, NextRunAt={}, Error={}",
                deliveryId, retry.getNextRunAt(), errorMessage);
    }

    /**
     * 재시도 성공 시 실행
     * - 재시도 상태 = COMPLETED
     * - 웹훅 성공 처리 (재고 차감 포함)
     */
    @Transactional
    public void markRetryAsSuccess(DeliveryWebhookRegisterRetry retry) {
        Delivery delivery = deliveryRepository.findById(retry.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("배송 정보 없음: " + retry.getTargetId()));

        // 재시도 상태 완료
        retry.setStatus(RetryStatus.COMPLETED);
        retryRepository.save(retry);

        // 웹훅 성공 처리 (재고 차감 포함)
        markWebhookAsSuccess(delivery.getDeliveryId(), delivery.getOrderId());

        log.info("재시도 성공: DeliveryId={}, RetryId={}", delivery.getDeliveryId(), retry.getId());
    }

    /**
     * 재시도 실패 시 실행
     * - 재시도 횟수 증가
     * - nextRunAt 업데이트 (지수 백오프)
     */
    @Transactional
    public void incrementRetry(DeliveryWebhookRegisterRetry retry, String errorMessage) {
        retry.setRetryCount(retry.getRetryCount() + 1);
        retry.setLastError(errorMessage);

        // 지수 백오프: 5분 → 10분 → 20분
        int delayMinutes = 5 * (int) Math.pow(2, retry.getRetryCount() - 1);
        retry.setNextRunAt(LocalDateTime.now().plusMinutes(delayMinutes));

        retryRepository.save(retry);

        log.warn("재시도 실패 ({}회): DeliveryId={}, NextRunAt={}",
                retry.getRetryCount(), retry.getTargetId(), retry.getNextRunAt());
    }

    /**
     * 재시도 최종 실패 시 실행
     * - webhookStatus = FAILED
     * - 재시도 상태 = EXHAUSTED
     */
    @Transactional
    public void markRetryAsExhausted(DeliveryWebhookRegisterRetry retry) {
        Delivery delivery = deliveryRepository.findById(retry.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("배송 정보 없음: " + retry.getTargetId()));

        // 배송 상태 실패로 변경
        delivery.setWebhookStatus(DeliveryWebhookStatus.FAILED);
        deliveryRepository.save(delivery);

        // 재시도 상태 소진
        retry.setStatus(RetryStatus.EXHAUSTED);
        retryRepository.save(retry);

        log.error("재시도 최종 실패: DeliveryId={}, RetryCount={}",
                delivery.getDeliveryId(), retry.getRetryCount());
    }
}
