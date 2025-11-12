package com.pcgear.complink.pcgear.config;

// 1. [수정] Page, PageImpl, PageRequest, Sort 등 Mixin 관련 import 모두 제거
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

        // 📌 [수정] private 헬퍼 메서드로 격리 (Spring MVC 오염 방지)
        private ObjectMapper buildRedisObjectMapper() {
                PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .build();

                ObjectMapper objectMapper = new ObjectMapper()
                                .findAndRegisterModules(); // ⬅️ PageModule 등록 제거

                // 📌 TodaySummary의 LinkedHashMap 오류를 해결하기 위해 이 설정은 유지
                objectMapper.activateDefaultTyping(
                                ptv,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return objectMapper;
        }

        // [수정] RedisTemplate 설정 (buildRedisObjectMapper() 호출)
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
                redisTemplate.setConnectionFactory(connectionFactory);
                redisTemplate.setKeySerializer(new StringRedisSerializer());

                redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(buildRedisObjectMapper()));

                redisTemplate.setHashKeySerializer(new StringRedisSerializer());
                redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(buildRedisObjectMapper()));
                redisTemplate.afterPropertiesSet();
                return redisTemplate;
        }

        // [수정] CacheManager 설정 (buildRedisObjectMapper() 호출)
        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(
                                buildRedisObjectMapper());

                return RedisCacheConfiguration.defaultCacheConfig()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonRedisSerializer));
        }

        // 📌 [수정] PageModule, PageImplMixin, PageRequestMixin, SortMixin 클래스 정의 (전부 삭제)
        // (모두 삭제합니다)

        // TTL 설정 (변경 없음)
        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
                        RedisCacheConfiguration cacheConfiguration) {

                return (builder) -> {
                        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

                        configurations.put("items", cacheConfiguration
                                        .entryTtl(Duration.ofHours(1)));
                        configurations.put("dashboard-summary", cacheConfiguration
                                        .entryTtl(Duration.ofHours(12)));

                        builder.withInitialCacheConfigurations(configurations);
                };
        }
}