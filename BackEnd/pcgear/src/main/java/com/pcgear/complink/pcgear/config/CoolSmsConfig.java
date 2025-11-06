package com.pcgear.complink.pcgear.config;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoolSmsConfig {

        // Spring이 properties 파일에서 값을 읽어 필드에 주입합니다.
        @Value("${coolsms.api.key}")
        private String apiKey;

        @Value("${coolsms.api.secret.key}")
        private String apiSecret;

        // CoolSMS SDK를 초기화하고 그 결과를 Bean으로 등록합니다.
        @Bean
        public DefaultMessageService defaultMessageService() {
                return NurigoApp.INSTANCE.initialize(
                                this.apiKey,
                                this.apiSecret,
                                "https://api.coolsms.co.kr");
        }
}
