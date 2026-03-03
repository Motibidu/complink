package com.pcgear.complink.pcgear.Order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_compensation_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedCompensationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompensationType compensationType;

    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private LocalDateTime nextRetryAt;

    @Column(length = 1000)
    private String errorMessage;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime completedAt;

    public void incrementRetryCount() {
        this.retryCount++;
        // 지수 백오프: 5분, 10분, 20분, 40분, 80분
        this.nextRetryAt = LocalDateTime.now().plusMinutes(5L * (long) Math.pow(2, this.retryCount - 1));
    }

    public enum CompensationType {
        ORDER_CANCELLATION,  // 주문 취소 보상
        ASSEMBLY_COMPLETION  // 조립 완료 보상
    }

    public enum JobStatus {
        PENDING,     // 대기 중
        PROCESSING,  // 처리 중
        COMPLETED,   // 완료
        FAILED       // 최종 실패 (관리자 개입 필요)
    }
}
