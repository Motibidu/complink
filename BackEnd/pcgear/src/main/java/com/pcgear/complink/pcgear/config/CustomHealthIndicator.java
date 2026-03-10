package com.pcgear.complink.pcgear.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 커스텀 헬스체크 인디케이터
 * - Database 연결 상태
 * - Redis 연결 상태
 * - 메모리 사용률
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    public CustomHealthIndicator(DataSource dataSource, RedisTemplate<String, Object> redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            // Database 연결 체크
            boolean dbHealthy = checkDatabase();

            // Redis 연결 체크
            boolean redisHealthy = checkRedis();

            // 메모리 사용률 체크
            double memoryUsage = checkMemoryUsage();

            if (!dbHealthy) {
                return Health.down()
                        .withDetail("reason", "Database connection failed")
                        .build();
            }

            if (!redisHealthy) {
                return Health.down()
                        .withDetail("reason", "Redis connection failed")
                        .build();
            }

            if (memoryUsage > 90.0) {
                return Health.outOfService()
                        .withDetail("reason", "Memory usage too high")
                        .withDetail("memoryUsage", memoryUsage + "%")
                        .build();
            }

            if (memoryUsage > 80.0) {
                return Health.up()
                        .withDetail("warning", "Memory usage is high")
                        .withDetail("memoryUsage", memoryUsage + "%")
                        .withDetail("database", "UP")
                        .withDetail("redis", "UP")
                        .build();
            }

            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("redis", "UP")
                    .withDetail("memoryUsage", memoryUsage + "%")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2초 타임아웃
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private double checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return (double) usedMemory / totalMemory * 100;
    }
}
