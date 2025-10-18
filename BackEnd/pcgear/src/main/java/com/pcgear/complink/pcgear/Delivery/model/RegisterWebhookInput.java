package com.pcgear.complink.pcgear.Delivery.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterWebhookInput {
        private String carrierId;
        private String trackingNumber;
        private String callbackUrl;
        private String expirationTime;
}
