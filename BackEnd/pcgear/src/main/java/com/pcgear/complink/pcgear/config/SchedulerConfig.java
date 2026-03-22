package com.pcgear.complink.pcgear.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 스케줄러 스레드 풀 설정
 *
 * 현재 스케줄러:
 * 1. InventoryReconciliationService (매일 새벽 3시)
 * 2. CompensationRetryScheduler (5분마다)
 *
 * 스레드 풀 크기 결정 기준:
 * - 기본 원칙: 동시 실행 가능한 스케줄러 개수 + 버퍼
 * - 현재: 2개 스케줄러가 겹칠 수 있으므로 최소 2개 필요
 * - 권장: 3~5개 (여유분 포함)
 */
@Slf4j
@Configuration
@Profile("!dev")
public class SchedulerConfig implements SchedulingConfigurer {

    private static final int POOL_SIZE = 3; // 스케줄러 개수 2개 + 여유 1개

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 스레드 풀 크기 설정
        scheduler.setPoolSize(POOL_SIZE);

        // 스레드 이름 prefix 설정 (로그에서 구분하기 쉽게)
        scheduler.setThreadNamePrefix("scheduled-task-");

        // 애플리케이션 종료 시 실행 중인 작업 완료 대기
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60); // 최대 60초 대기

        // 스레드가 데몬 스레드가 아님 (중요한 작업이므로)
        scheduler.setDaemon(false);

        // 작업 거부 시 정책 설정 (큐가 가득 차면 현재 스레드에서 실행)
        scheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);

        log.info("스케줄러 스레드 풀 초기화 완료 - PoolSize: {}", POOL_SIZE);
    }
}
