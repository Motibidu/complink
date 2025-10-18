package com.pcgear.complink.pcgear.KJG.user.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// UserNotApprovedAuthenticationException이 exception 패키지에 있다면 해당 경로로 수정 필요
import com.pcgear.complink.pcgear.KJG.user.exception.UserNotApprovedAuthenticationException;
import com.pcgear.complink.pcgear.KJG.user.exception.UserNotFoundException;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        // HTTP 응답 헤더 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); 
        response.setHeader("Access-Control-Allow-Credentials", "true");

        String errorMessage = "로그인 정보가 일치하지 않습니다. (아이디/비밀번호 오류)";
        int httpStatus= HttpServletResponse.SC_UNAUTHORIZED;
        
        // 실제 발생한 예외 클래스를 출력하여 디버깅에 활용
        System.out.println("실제 발생 예외 타입: " + exception.getClass().getSimpleName());
        System.out.println("CustomAuthFailureHandler - Cause 예외 타입: " + (exception.getCause() != null ? exception.getCause().getClass().getSimpleName() : "없음 (null)"));

        // 1. InternalAuthenticationServiceException 처리 (미승인 사용자일 가능성)
        if (exception instanceof InternalAuthenticationServiceException) {
            
            Throwable cause = exception.getCause();
            
            if (cause instanceof UserNotApprovedAuthenticationException) {
                // 승인 대기 중인 사용자 (우리가 정의한 예외가 Cause로 감싸져서 전달됨)
                httpStatus = HttpServletResponse.SC_FORBIDDEN; // 403
                errorMessage = "회원가입 승인이 아직 완료되지 않았습니다. 관리자에게 문의하세요.";
            } else if(cause instanceof UserNotFoundException){
                httpStatus = HttpServletResponse.SC_NOT_FOUND; // 404
                errorMessage = "아이디를 찾을 수 없습니다.";
            }
        
        // 2. BadCredentialsException 처리 (ID 없음 또는 비밀번호 불일치)
        } else if (exception instanceof BadCredentialsException) {
            // Spring Security가 UsernameNotFoundException을 BadCredentialsException으로 마스킹합니다.
            // 보안상 ID 없음과 비밀번호 불일치를 구분하지 않고 401을 반환합니다.
            httpStatus = HttpServletResponse.SC_UNAUTHORIZED; // 401
            errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
            
        }
        
        response.setStatus(httpStatus);
        
        // 클라이언트에게 JSON 형태로 응답 (예외 타입 정보를 포함)
        String jsonResponse = String.format("{\"status\": %d, \"message\": \"%s\", \"exceptionType\": \"%s\"}", 
                                            httpStatus, 
                                            errorMessage, 
                                            exception.getClass().getSimpleName());
        response.getWriter().write(jsonResponse);
    }
}
