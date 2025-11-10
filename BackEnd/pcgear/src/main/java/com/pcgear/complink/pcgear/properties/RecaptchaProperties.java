package com.pcgear.complink.pcgear.properties;

import org.springframework.beans.factory.annotation.Value; // Spring의 @Value 사용
import org.springframework.stereotype.Component; // @Component 추가

import lombok.Getter;
import lombok.ToString;

@Component // 스프링 빈으로 등록하여 @Value 주입을 받게 함
@Getter
@ToString
// @ConfigurationProperties(prefix = "recaptcha") 제거
public class RecaptchaProperties {

        private final String secretKey;
        private final String verifyUrl;

        // @Value로 속성 키를 명시
        public RecaptchaProperties(
                        @Value("${recaptcha.secret-key}") String secretKey,
                        @Value("${recaptcha.verify-url}") String verifyUrl) {

                this.secretKey = secretKey;
                this.verifyUrl = verifyUrl;
        }
}