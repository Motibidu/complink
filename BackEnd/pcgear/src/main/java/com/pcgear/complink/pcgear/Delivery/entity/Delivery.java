package com.pcgear.complink.pcgear.Delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "delivery")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Delivery {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer deliveryId;

        private String carrierId;

        @Column(unique = true)
        private String trackingNumber;

        private Integer orderId;

        private String customerId;

        private String recipientName;

        private String recipientPhone;

        private String recipientAddr;

        private String currentStatus;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        private LocalDateTime completedAt;

        // 엔티티 생성 시 자동으로 created_at과 currentStatus를 설정합니다.
        @PrePersist
        protected void onCreate() {
                this.createdAt = LocalDateTime.now();
                if (this.currentStatus == null) {
                        this.currentStatus = "접수"; // 기본 상태
                }
        }
}
