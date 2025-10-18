package com.pcgear.complink.pcgear.KJG.user.exception;

// 404 Not Found를 위한 예외
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
