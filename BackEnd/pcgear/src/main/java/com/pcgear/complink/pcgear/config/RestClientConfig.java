package com.pcgear.complink.pcgear.config;

import java.time.Duration;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

        @Bean
        public RestClient restClient() {
                // 타임아웃 설정
                ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(Duration.ofSeconds(5))  // 연결 타임아웃: 5초
                        .withReadTimeout(Duration.ofSeconds(10));   // 읽기 타임아웃: 10초

                ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

                return RestClient.builder()
                        .requestFactory(requestFactory)
                        .build();
        }
}
