package com.pcgear.complink.pcgear.Order.controller;

import com.pcgear.complink.pcgear.Order.model.FailedCompensationJob;
import com.pcgear.complink.pcgear.Order.repository.FailedCompensationJobRepository;
import com.pcgear.complink.pcgear.Order.scheduler.CompensationRetryScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/compensation-jobs")
@RequiredArgsConstructor
@Tag(name = "보상 트랜잭션 관리", description = "실패한 보상 트랜잭션 조회 및 수동 재시도 API")
public class CompensationJobController {

    private final FailedCompensationJobRepository failedJobRepository;
    private final CompensationRetryScheduler retryScheduler;

    @Operation(summary = "모든 보상 작업 조회", description = "상태별로 보상 작업 목록 조회")
    @GetMapping
    public ResponseEntity<List<FailedCompensationJob>> getAllJobs(
            @RequestParam(required = false) FailedCompensationJob.JobStatus status) {

        List<FailedCompensationJob> jobs = status != null
            ? failedJobRepository.findByStatus(status)
            : failedJobRepository.findAll();

        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "최종 실패 작업 조회", description = "수동 처리가 필요한 최종 실패 작업 조회")
    @GetMapping("/failed")
    public ResponseEntity<List<FailedCompensationJob>> getFailedJobs() {
        List<FailedCompensationJob> failedJobs = failedJobRepository.findByStatus(
            FailedCompensationJob.JobStatus.FAILED
        );
        return ResponseEntity.ok(failedJobs);
    }

    @Operation(summary = "특정 작업 상세 조회", description = "작업 ID로 상세 정보 조회")
    @GetMapping("/{jobId}")
    public ResponseEntity<FailedCompensationJob> getJob(@PathVariable Long jobId) {
        return failedJobRepository.findById(jobId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "수동 재시도", description = "관리자가 특정 작업을 수동으로 재시도")
    @PostMapping("/{jobId}/retry")
    public ResponseEntity<String> manualRetry(@PathVariable Long jobId) {
        try {
            log.info("관리자 수동 재시도 요청 - JobId: {}", jobId);
            retryScheduler.manualRetry(jobId);
            return ResponseEntity.ok("재시도 완료");
        } catch (Exception e) {
            log.error("수동 재시도 실패 - JobId: {}, Error: {}", jobId, e.getMessage());
            return ResponseEntity.badRequest().body("재시도 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "작업 삭제", description = "완료되거나 더 이상 필요 없는 작업 삭제")
    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId) {
        failedJobRepository.deleteById(jobId);
        log.info("보상 작업 삭제 - JobId: {}", jobId);
        return ResponseEntity.ok("삭제 완료");
    }
}
