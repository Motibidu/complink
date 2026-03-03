package com.pcgear.complink.pcgear.Order.repository;

import com.pcgear.complink.pcgear.Order.model.FailedCompensationJob;
import com.pcgear.complink.pcgear.Order.model.FailedCompensationJob.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FailedCompensationJobRepository extends JpaRepository<FailedCompensationJob, Long> {

    /**
     * 재시도 대상 작업 조회
     * - 상태가 PENDING
     * - 다음 재시도 시각이 현재 시각보다 이전
     */
    @Query("SELECT j FROM FailedCompensationJob j " +
           "WHERE j.status = :status " +
           "AND j.nextRetryAt <= :now " +
           "ORDER BY j.createdAt ASC")
    List<FailedCompensationJob> findPendingJobsForRetry(@Param("status") JobStatus status, @Param("now") LocalDateTime now);

    /**
     * 특정 주문의 특정 타입 작업이 이미 존재하는지 확인
     */
    boolean existsByOrderIdAndCompensationTypeAndStatusNot(
        Integer orderId,
        FailedCompensationJob.CompensationType compensationType,
        JobStatus status
    );

    /**
     * 최종 실패한 작업 조회 (관리자 확인용)
     */
    List<FailedCompensationJob> findByStatus(JobStatus status);
}
