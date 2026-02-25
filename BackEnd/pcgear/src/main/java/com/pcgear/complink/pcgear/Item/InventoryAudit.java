package com.pcgear.complink.pcgear.Item;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 재고 정합성 검증 감사 로그
 */
@Entity
@Table(name = "inventory_audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InventoryAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * 수정 전 가용재고
     */
    @Column(nullable = false)
    private Integer previousAvailableQuantity;

    /**
     * 수정 후 가용재고
     */
    @Column(nullable = false)
    private Integer correctedAvailableQuantity;

    /**
     * 차이 (불일치 수량)
     */
    @Column(nullable = false)
    private Integer discrepancy;

    /**
     * 검증 사유
     */
    @Column(length = 500)
    private String reason;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
