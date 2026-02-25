package com.pcgear.complink.pcgear.Item;

import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 재고 정합성 검증 서비스
 * Spring Batch 대신 간단한 @Scheduled 사용
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryReconciliationService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final InventoryAuditRepository auditRepository;

    /**
     * 예약된 재고 상태 (주문 생성 ~ 운송장 등록 전)
     *
     * 운송장 등록 시점에 실재고가 차감되므로,
     * SHIPPING_PENDING, SHIPPING 상태는 예약재고에서 제외
     */
    private static final List<OrderStatus> RESERVED_STATUSES = List.of(
            OrderStatus.ORDER_RECEIVED,
            OrderStatus.PAYMENT_PENDING,
            OrderStatus.PAID,
            OrderStatus.PREPARING_PRODUCT
    );

    /**
     * 매일 새벽 3시에 전체 재고 정합성 검증
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void reconcileAllInventory() {
        log.info("=== 전체 재고 정합성 검증 시작 ===");

        List<Item> items = itemRepository.findAll();
        int fixedCount = 0;
        int totalCount = items.size();

        for (Item item : items) {
            try {
                if (reconcileSingleItem(item)) {
                    fixedCount++;
                }
            } catch (Exception e) {
                log.error("재고 정합성 검증 실패. ItemId: {}, Error: {}", item.getItemId(), e.getMessage(), e);
            }
        }

        log.info("=== 전체 재고 정합성 검증 완료 === 수정: {} / 전체: {}", fixedCount, totalCount);
    }

    /**
     * 특정 품목 재고 정합성 검증 (수동 실행용)
     */
    @Transactional
    public boolean reconcileSingleItem(Integer itemId) {
        log.info("단일 품목 재고 정합성 검증 시작. ItemId: {}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("품목을 찾을 수 없습니다. ID: " + itemId));

        return reconcileSingleItem(item);
    }

    /**
     * 품목의 재고 정합성 검증 및 수정
     */
    private boolean reconcileSingleItem(Item item) {
        Integer calculatedAvailable = calculateCorrectAvailableQuantity(item);
        Integer currentAvailable = item.getAvailableQuantity();

        // 불일치 발견
        if (!calculatedAvailable.equals(currentAvailable)) {
            Integer discrepancy = currentAvailable - calculatedAvailable;

            log.warn("재고 불일치 발견 - ItemId: {}, ItemName: {}, 현재: {}, 계산값: {}, 차이: {}",
                    item.getItemId(), item.getItemName(), currentAvailable, calculatedAvailable, discrepancy);

            // 가용재고 수정
            item.setAvailableQuantity(calculatedAvailable);
            itemRepository.save(item);

            // 감사 로그 저장
            saveAuditLog(item, currentAvailable, calculatedAvailable, discrepancy, "자동 정합성 검증");

            log.info("재고 수정 완료. ItemId: {}", item.getItemId());
            return true;
        }

        return false;
    }

    /**
     * 올바른 가용재고 계산
     * 공식: 가용재고 = 실재고 - 예약재고
     */
    private Integer calculateCorrectAvailableQuantity(Item item) {
        Integer quantityOnHand = item.getQuantityOnHand();

        Integer reservedQuantity = orderRepository.calculateReservedQuantityByItemId(
                item.getItemId(),
                RESERVED_STATUSES
        );

        return quantityOnHand - reservedQuantity;
    }

    /**
     * 감사 로그 저장
     */
    private void saveAuditLog(Item item, Integer previous, Integer corrected, Integer discrepancy, String reason) {
        InventoryAudit audit = InventoryAudit.builder()
                .item(item)
                .previousAvailableQuantity(previous)
                .correctedAvailableQuantity(corrected)
                .discrepancy(discrepancy)
                .reason(reason)
                .build();

        auditRepository.save(audit);
    }

    /**
     * 특정 품목의 감사 로그 조회
     */
    @Transactional(readOnly = true)
    public List<InventoryAudit> getAuditLogs(Integer itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("품목을 찾을 수 없습니다. ID: " + itemId));

        return auditRepository.findByItemOrderByCreatedAtDesc(item);
    }
}
