package com.pcgear.complink.pcgear.PJH.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 메시징을 사용하는 웹소켓 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트(React)에서 웹소켓에 연결할 때 사용할 엔드포인트
        // "/ws"로 설정하고, 모든 도메인에서의 연결을 허용(CORS)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // "/topic"으로 시작하는 경로로 메시지를 라우팅
        registry.enableSimpleBroker("/topic");
        // 클라이언트에서 서버로 메시지를 보낼 때 사용하는 접두사
        registry.setApplicationDestinationPrefixes("/api");
    }
}
