package com.pcgear.complink.pcgear.Order.scheduler;

import com.pcgear.complink.pcgear.Order.model.FailedCompensationJob;
import com.pcgear.complink.pcgear.Order.repository.FailedCompensationJobRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationRetryScheduler {

    private final FailedCompensationJobRepository failedJobRepository;
    private final OrderService orderService;

    private static final int MAX_RETRY_COUNT = 5;

    /**
     * 5분마다 실패한 보상 트랜잭션 재시도
     * - PENDING 상태이고 nextRetryAt이 현재 시각보다 이전인 작업들을 조회
     * - 각 작업을 재시도하고 성공하면 COMPLETED, 실패하면 retryCount 증가
     * - 최대 재시도 횟수 초과 시 FAILED로 변경 및 관리자 알림
     */
    @Scheduled(fixedDelay = 300000) // 5분 (300,000ms)
    @Transactional
    public void retryFailedCompensations() {
        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();
        log.info("=== 보상 트랜잭션 재시도 스케줄러 시작 === Thread: {}", threadName);

        List<FailedCompensationJob> pendingJobs = failedJobRepository.findPendingJobsForRetry(
            FailedCompensationJob.JobStatus.PENDING,
            LocalDateTime.now()
        );

        if (pendingJobs.isEmpty()) {
            log.info("재시도할 작업이 없습니다.");
            return;
        }

        log.info("재시도 대상 작업 수: {}", pendingJobs.size());

        for (FailedCompensationJob job : pendingJobs) {
            processCompensationJob(job);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("=== 보상 트랜잭션 재시도 스케줄러 종료 === 소요시간: {}ms", duration);
    }

    private void processCompensationJob(FailedCompensationJob job) {
        log.info("보상 작업 재시도 시작 - JobId: {}, OrderId: {}, Type: {}, RetryCount: {}",
            job.getId(), job.getOrderId(), job.getCompensationType(), job.getRetryCount());

        // 최대 재시도 횟수 체크
        if (job.getRetryCount() >= MAX_RETRY_COUNT) {
            markAsFailed(job);
            return;
        }

        // 작업 상태를 PROCESSING으로 변경 (동시성 방지)
        job.setStatus(FailedCompensationJob.JobStatus.PROCESSING);
        failedJobRepository.save(job);

        try {
            // 보상 트랜잭션 실행
            executeCompensation(job);

            // 성공 시 COMPLETED로 변경
            job.setStatus(FailedCompensationJob.JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            failedJobRepository.save(job);

            log.info("✅ 보상 작업 재시도 성공 - JobId: {}, OrderId: {}", job.getId(), job.getOrderId());

        } catch (Exception e) {
            // 실패 시 재시도 카운트 증가 및 다음 재시도 시간 설정
            job.incrementRetryCount();
            job.setStatus(FailedCompensationJob.JobStatus.PENDING);
            job.setErrorMessage(e.getMessage());
            failedJobRepository.save(job);

            log.error("❌ 보상 작업 재시도 실패 - JobId: {}, OrderId: {}, RetryCount: {}/{}, NextRetry: {}, Error: {}",
                job.getId(), job.getOrderId(), job.getRetryCount(), MAX_RETRY_COUNT,
                job.getNextRetryAt(), e.getMessage());
        }
    }

    private void executeCompensation(FailedCompensationJob job) {
        switch (job.getCompensationType()) {
            case ORDER_CANCELLATION:
                orderService.compensateOrderCancellation(job.getOrderId());
                break;
            case ASSEMBLY_COMPLETION:
                orderService.compensateAssemblyCompletion(job.getOrderId());
                break;
            default:
                throw new IllegalArgumentException("알 수 없는 보상 타입: " + job.getCompensationType());
        }
    }

    private void markAsFailed(FailedCompensationJob job) {
        job.setStatus(FailedCompensationJob.JobStatus.FAILED);
        failedJobRepository.save(job);

        // TODO: 관리자에게 긴급 알림 전송 (Slack, Email 등)
        log.error("🚨 보상 트랜잭션 최종 실패 - 수동 처리 필요! JobId: {}, OrderId: {}, Type: {}, RetryCount: {}/{}",
            job.getId(), job.getOrderId(), job.getCompensationType(), job.getRetryCount(), MAX_RETRY_COUNT);

        // 여기에 알림 서비스 호출 추가
        // alertService.sendCriticalAlert("보상 트랜잭션 최종 실패", job);
    }

    /**
     * 관리자용 API: 수동으로 특정 작업 재시도
     */
    @Transactional
    public void manualRetry(Long jobId) {
        FailedCompensationJob job = failedJobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("작업을 찾을 수 없습니다. JobId: " + jobId));

        log.info("수동 재시도 요청 - JobId: {}, OrderId: {}", job.getId(), job.getOrderId());
        processCompensationJob(job);
    }
}
