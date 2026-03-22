package com.pcgear.complink.pcgear.Delivery.entity;

import java.time.LocalDateTime;

import com.pcgear.complink.pcgear.Delivery.enums.RetryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DeliveryWebhookRegisterRetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long targetId; // Tracking 테이블의 ID

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON 형태의 요청 데이터

    private int retryCount = 0;
    private int maxRetries = 10;
    private LocalDateTime nextRunAt;

    @Enumerated(EnumType.STRING)
    private RetryStatus status = RetryStatus.ACTIVE;

    private String lastError;

    // 재시도 횟수에 따른 다음 실행 시간 계산 (지수 백오프)
    public void updateNextRunTime() {
        this.retryCount++;
        // 2의 n승 분 단위로 지연 (5분, 10분, 20분...)
        long delayMinutes = (long) Math.pow(2, this.retryCount) * 5;
        this.nextRunAt = LocalDateTime.now().plusMinutes(delayMinutes);

        if (this.retryCount >= this.maxRetries) {
            this.status = RetryStatus.EXHAUSTED;
        }
    }
}
