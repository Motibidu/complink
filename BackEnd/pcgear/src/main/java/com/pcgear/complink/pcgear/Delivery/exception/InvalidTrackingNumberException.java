package com.pcgear.complink.pcgear.Delivery.exception;

/**
 * 운송장 번호 검증 실패 예외
 */
public class InvalidTrackingNumberException extends DeliveryTrackingException {

    public InvalidTrackingNumberException(String message) {
        super(message);
    }
}
