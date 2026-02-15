package com.pcgear.complink.pcgear.Delivery.exception;

/**
 * 웹훅 등록 실패 예외
 */
public class WebhookRegistrationException extends DeliveryTrackingException {

    public WebhookRegistrationException(String message) {
        super(message);
    }

    public WebhookRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
