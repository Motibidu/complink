package com.pcgear.complink.pcgear.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus 메트릭 수집 설정
 * 비즈니스 메트릭을 수집하여 Grafana에서 모니터링
 */
@Configuration
public class MetricsConfig {

    /**
     * 주문 생성 카운터
     */
    @Bean
    public Counter orderCreatedCounter(MeterRegistry registry) {
        return Counter.builder("business.order.generated")
                .description("총 주문 생성 수")
                .tag("type", "order")
                .register(registry);
    }

    /**
     * 주문 실패 카운터
     */
    @Bean
    public Counter orderFailedCounter(MeterRegistry registry) {
        return Counter.builder("business.order.failed")
                .description("주문 실패 수")
                .tag("type", "order")
                .register(registry);
    }

    /**
     * 결제 성공 카운터
     */
    @Bean
    public Counter paymentSuccessCounter(MeterRegistry registry) {
        return Counter.builder("business.payment.success")
                .description("결제 성공 수")
                .tag("type", "payment")
                .register(registry);
    }

    /**
     * 결제 실패 카운터
     */
    @Bean
    public Counter paymentFailedCounter(MeterRegistry registry) {
        return Counter.builder("business.payment.failed")
                .description("결제 실패 수")
                .tag("type", "payment")
                .register(registry);
    }

    /**
     * 재고 부족 카운터
     */
    @Bean
    public Counter stockOutCounter(MeterRegistry registry) {
        return Counter.builder("business.stock.out")
                .description("재고 부족 발생 수")
                .tag("type", "inventory")
                .register(registry);
    }

    /**
     * 외부 API 호출 타이머 (배송, 결제 등)
     */
    @Bean
    public Timer externalApiTimer(MeterRegistry registry) {
        return Timer.builder("business.external.api.call")
                .description("외부 API 호출 시간")
                .tag("type", "external")
                .register(registry);
    }

    /**
     * 보상 트랜잭션 재시도 카운터
     */
    @Bean
    public Counter compensationRetryCounter(MeterRegistry registry) {
        return Counter.builder("business.compensation.retry")
                .description("보상 트랜잭션 재시도 수")
                .tag("type", "compensation")
                .register(registry);
    }

    /**
     * 재고 정합성 검증 실패 카운터
     */
    @Bean
    public Counter inventoryReconciliationFailCounter(MeterRegistry registry) {
        return Counter.builder("business.inventory.reconciliation.fail")
                .description("재고 정합성 검증 실패 수")
                .tag("type", "inventory")
                .register(registry);
    }
}
