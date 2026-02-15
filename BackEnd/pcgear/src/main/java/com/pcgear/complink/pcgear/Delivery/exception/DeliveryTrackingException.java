package com.pcgear.complink.pcgear.Delivery.exception;

/**
 * 배송 추적 등록 중 발생하는 예외
 */
public class DeliveryTrackingException extends RuntimeException {

    public DeliveryTrackingException(String message) {
        super(message);
    }

    public DeliveryTrackingException(String message, Throwable cause) {
        super(message, cause);
    }
}
