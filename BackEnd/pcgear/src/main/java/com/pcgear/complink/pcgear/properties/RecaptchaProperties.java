package com.pcgear.complink.pcgear.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.ToString;

@ConfigurationProperties(prefix = "recaptcha")
@Getter
@ToString
public class RecaptchaProperties {
        private final String secretKey;
        private final String verifyUrl;

        public RecaptchaProperties(String secretKey, String verifyUrl) {
                this.secretKey = secretKey;
                this.verifyUrl = verifyUrl;
        }
}
