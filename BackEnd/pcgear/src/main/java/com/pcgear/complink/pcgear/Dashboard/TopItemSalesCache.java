package com.pcgear.complink.pcgear.Dashboard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "top_items_sales_cache", indexes = {
        @Index(name = "idx_cache_rank", columnList = "rank_position")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopItemSalesCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer itemId;

    private String itemName;

    private Integer totalQuantity;

    private Long totalRevenue;

    private Integer availableQuantity;

    private Integer rankPosition;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    /**
     * Entity를 DTO로 변환
     */
    public TopItemSalesDto toDto() {
        return new TopItemSalesDto(
                this.itemId,
                this.itemName,
                this.totalQuantity,
                this.totalRevenue,
                this.availableQuantity
        );
    }
}
