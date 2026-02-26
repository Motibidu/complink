package com.pcgear.complink.pcgear.Item.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 재고 불일치 감지 이벤트
 */
@Getter
@RequiredArgsConstructor
public class InventoryDiscrepancyEvent {
    private final Integer itemId;
    private final String itemName;
    private final Integer previousQuantity;
    private final Integer correctedQuantity;
    private final Integer discrepancy;
}
