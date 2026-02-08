package com.pcgear.complink.pcgear.exception;

/**
 * 환불은 성공했으나 DB 업데이트 실패 등 데이터 정합성이 깨졌을 때 발생하는 예외
 */
public class InconsistentDataException extends RuntimeException {
    public InconsistentDataException(String message) {
        super(message);
    }
}
