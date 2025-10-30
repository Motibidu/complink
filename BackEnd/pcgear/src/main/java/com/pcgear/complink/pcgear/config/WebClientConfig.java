package com.pcgear.complink.pcgear.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration // 이 클래스가 스프링 설정 클래스임을 알립니다.
public class WebClientConfig {

        @Bean // 이 메서드가 반환하는 객체를 스프링 빈으로 등록합니다.
        public WebClient webClient() {
                // 기본 WebClient 인스턴스를 생성합니다.
                // 여기에 타임아웃, 베이스 URL, 디폴트 헤더 등 커스터마이징을 추가할 수 있습니다.
                return WebClient.builder().build();
        }
}
