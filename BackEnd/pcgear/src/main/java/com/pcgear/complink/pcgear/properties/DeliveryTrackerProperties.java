package com.pcgear.complink.pcgear.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.ToString;

@Component // 스프링이 이 클래스를 설정 빈으로 로드하게 함
@Getter
@ToString
// @ConfigurationProperties, @ConstructorBinding 제거

public class DeliveryTrackerProperties {
        // 필드는 final로 유지하여 불변성 유지

        private final String clientId;
        private final String clientSecret;
        private final String authUrl;
        private final String graphqlApiUrl;
        private final String webhookUrl;

        // 명시적인 생성자를 정의하여 @Value로 직접 주입
        public DeliveryTrackerProperties(
                        @Value("${delivery-tracker.client-id}") String clientId,
                        @Value("${delivery-tracker.client-secret}") String clientSecret,
                        @Value("${delivery-tracker.auth-url}") String authUrl,
                        @Value("${delivery-tracker.graphql-api-url}") String graphqlApiUrl,
                        @Value("${webhook-url}") String webhookUrl) {

                // 필드 초기화는 그대로 유지
                this.clientId = clientId;
                this.clientSecret = clientSecret;
                this.authUrl = authUrl;
                this.graphqlApiUrl = graphqlApiUrl;
                this.webhookUrl = webhookUrl;
        }
}
