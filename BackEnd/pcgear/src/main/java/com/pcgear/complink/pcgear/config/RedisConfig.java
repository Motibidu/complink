package com.pcgear.complink.pcgear.config;

import org.springframework.cache.annotation.EnableCaching; // 1. import 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration; // 2. import 추가
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext; // 3. import 추가
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // 4. @Cacheable, @CacheEvict 등 캐시 어노테이션을 활성화합니다.
public class RedisConfig {

        // 이 빈은 수동으로 RedisTemplate을 주입받아 사용할 때 적용됩니다.
        // (지금 문제와는 직접적 관련이 없었지만, 잘 설정하셨습니다.)
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
                redisTemplate.setConnectionFactory(connectionFactory);
                redisTemplate.setKeySerializer(new StringRedisSerializer());
                redisTemplate.setHashKeySerializer(new StringRedisSerializer());
                redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
                redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
                redisTemplate.afterPropertiesSet();
                return redisTemplate;
        }

        // 5. [핵심] @Cacheable이 사용할 CacheManager의 설정을 정의합니다.
        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                return RedisCacheConfiguration.defaultCacheConfig()
                                // Key Serializer는 String으로 설정 (Redis CLI에서 보기 편함)
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                // Value Serializer는 JSON으로 설정 (여기서 SerializationException 해결)
                                      .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        }
}