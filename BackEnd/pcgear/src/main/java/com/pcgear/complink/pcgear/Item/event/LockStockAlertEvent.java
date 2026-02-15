package com.pcgear.complink.pcgear.Item.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LockStockAlertEvent {
        private final Integer itemId;
        private final String itemName;
        private final int currentQuantity;
        private final int orderedQuantity;

        public int getShortage() {
                return orderedQuantity - currentQuantity;
        }
}
