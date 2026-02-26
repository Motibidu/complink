package com.pcgear.complink.pcgear.Item.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.pcgear.complink.pcgear.config.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemEventListener {

        private final SseEmitterManager sseEmitterManager;

        @Async
        @EventListener
        public void handleStockInsufficiency(LockStockAlertEvent event) {
                try {
                        String notificationMessage = String.format(
                                        "⚠️ [재고 부족] %s - 현재: %d개, 주문: %d개, 부족: %d개",
                                        event.getItemName(),
                                        event.getCurrentQuantity(),
                                        event.getOrderedQuantity(),
                                        event.getShortage());

                        sseEmitterManager.broadcast(notificationMessage);
                        log.info("재고 부족 알림 전송: itemId={}, itemName={}, 현재={}개, 주문={}개, 부족={}개",
                                        event.getItemId(),
                                        event.getItemName(),
                                        event.getCurrentQuantity(),
                                        event.getOrderedQuantity(),
                                        event.getShortage());

                } catch (Exception e) {
                        log.error("재고 부족 알림 전송 실패 (재고 부족은 정상 처리됨): itemId={}, itemName={}, error={}",
                                        event.getItemId(), event.getItemName(), e.getMessage());
                }
        }

        @Async
        @EventListener
        public void handleInventoryDiscrepancy(InventoryDiscrepancyEvent event) {
                try {
                        String notificationMessage = String.format(
                                        "🔍 [재고 불일치] %s - 이전: %d개, 수정: %d개, 차이: %d개",
                                        event.getItemName(),
                                        event.getPreviousQuantity(),
                                        event.getCorrectedQuantity(),
                                        event.getDiscrepancy());

                        sseEmitterManager.broadcast(notificationMessage);
                        log.info("재고 불일치 알림 전송: itemId={}, itemName={}, 이전={}개, 수정={}개, 차이={}개",
                                        event.getItemId(),
                                        event.getItemName(),
                                        event.getPreviousQuantity(),
                                        event.getCorrectedQuantity(),
                                        event.getDiscrepancy());

                } catch (Exception e) {
                        log.error("재고 불일치 알림 전송 실패: itemId={}, itemName={}, error={}",
                                        event.getItemId(), event.getItemName(), e.getMessage());
                }
        }

}
