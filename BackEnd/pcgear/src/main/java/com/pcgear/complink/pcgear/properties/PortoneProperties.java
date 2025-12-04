package com.pcgear.complink.pcgear.properties;

import org.springframework.beans.factory.annotation.Value; // Spring의 @Value 사용
import org.springframework.stereotype.Component; // @Component 추가

import lombok.Getter;
import lombok.ToString;

@Component // 스프링 빈으로 등록하여 @Value 주입을 받게 함
@Getter
@ToString
// @ConfigurationProperties(prefix = "portone") 제거
public class PortoneProperties {

        private final String apiSecret;
        private final String webhookSecret;
        private final String webhookUrl;
        private final String apiUrl;
        private final String accessTokenUrl;
        private final String impKey;
        private final String impSecret;
        private final String impUserCode;

        // @ConstructorBinding 제거. @Value로 속성 키를 명시
        public PortoneProperties(
                        @Value("${portone.api-secret}") String apiSecret,
                        @Value("${portone.webhook-secret}") String webhookSecret,
                        @Value("${portone.webhook-url}") String webhookUrl,
                        @Value("${portone.api-url}") String apiUrl,
                        @Value("${portone.access-token-url}") String accessTokenUrl,
                        @Value("${portone.imp-key}") String impKey,
                        @Value("${portone.imp-secret}") String impSecret,
                        @Value("${portone.imp-usercode}") String impUserCode) {

                this.apiSecret = apiSecret;
                this.webhookSecret = webhookSecret;
                this.webhookUrl = webhookUrl;
                this.apiUrl = apiUrl;
                this.accessTokenUrl = accessTokenUrl;
                this.impKey = impKey;
                this.impSecret = impSecret;
                this.impUserCode = impUserCode;

        }
}