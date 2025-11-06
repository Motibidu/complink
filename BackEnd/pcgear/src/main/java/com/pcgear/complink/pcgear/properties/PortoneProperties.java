package com.pcgear.complink.pcgear.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;
import lombok.ToString;

@ConfigurationProperties(prefix = "portone")
@Getter
@ToString
public class PortoneProperties {
        private final String apiSecret;
        private final String webhookSecret;
        private final String webhookUrl;
        private final String apiUrl;
        private final String accessTokenUrl;
        private final String impKey;
        private final String impSecret;

        @ConstructorBinding
        public PortoneProperties(String apiSecret, String webhookSecret, String webhookUrl, String apiUrl,
                        String accessTokenUrl, String impKey, String impSecret) {
                this.apiSecret = apiSecret;
                this.webhookSecret = webhookSecret;
                this.webhookUrl = webhookUrl;
                this.apiUrl = apiUrl;
                this.accessTokenUrl = accessTokenUrl;
                this.impKey = impKey;
                this.impSecret = impSecret;
        }

}
