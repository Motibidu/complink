package com.pcgear.complink.pcgear.Dashboard;

import com.pcgear.complink.pcgear.Item.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStockDto {
    private ItemCategory category;
    private Long totalItems;
    private Long totalStock;
    private Long totalAvailable;
    private Integer totalValue; // 재고 총 가치 (재고 * 판매가)
}
