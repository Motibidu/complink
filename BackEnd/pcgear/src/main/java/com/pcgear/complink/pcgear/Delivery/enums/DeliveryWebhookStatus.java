package com.pcgear.complink.pcgear.Delivery.enums;


public enum DeliveryWebhookStatus {
        PENDING("PENDING"),
        SUCCESS("SUCCESS"),
        FAILED("FAILED");

        private final String description;

        DeliveryWebhookStatus(String description) {
                this.description = description;
        }

}
