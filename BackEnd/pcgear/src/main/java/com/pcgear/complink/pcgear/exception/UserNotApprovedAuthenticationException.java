package com.pcgear.complink.pcgear.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 사용자가 존재하지만, 관리자의 승인이 필요한 상태임을 나타내는 인증 예외입니다.
 * 이 예외는 CustomAuthFailureHandler에서 403 Forbidden 상태로 매핑됩니다.
 */
public class UserNotApprovedAuthenticationException extends AuthenticationException {
    public UserNotApprovedAuthenticationException(String msg) {
        super(msg);
    }

    public UserNotApprovedAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
