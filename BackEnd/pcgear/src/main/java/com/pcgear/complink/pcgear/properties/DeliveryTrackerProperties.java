package com.pcgear.complink.pcgear.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;
import lombok.ToString;

@ConfigurationProperties(prefix = "delivery-tracker")
@Getter
@ToString
public class DeliveryTrackerProperties {
        private final String clientId;
        private final String clientSecret;
        private final String authUrl;
        private final String graphqlApiUrl;
        private final String webhookUrl;

        @ConstructorBinding
        public DeliveryTrackerProperties(String clientId, String clientSecret, String authUrl, String graphqlApiUrl,
                        String webhookUrl) {
                this.clientId = clientId;
                this.clientSecret = clientSecret;
                this.authUrl = authUrl;
                this.graphqlApiUrl = graphqlApiUrl;
                this.webhookUrl = webhookUrl;
        }

}
