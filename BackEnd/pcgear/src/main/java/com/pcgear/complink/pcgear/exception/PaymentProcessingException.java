package com.pcgear.complink.pcgear.exception;

/**
 * 외부 결제 서비스 연동 등 결제 처리 과정에서 오류 발생 시 사용되는 예외
 */
public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
