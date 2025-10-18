package com.pcgear.complink.pcgear.KJG.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.exception.UserNotApprovedAuthenticationException;
import com.pcgear.complink.pcgear.KJG.user.exception.UserNotFoundException;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UserNotFoundException {

                UserEntity userEntity = userRepository.findByUsername(username)
                // Case 1: 존재하지 않는 사용자 ID (404)
                // Spring Security 표준 예외: UsernameNotFoundException을 던집니다.
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

        // Case 2: 승인 대기 중인 ID (403)
        // UserEntity에 isApproved() 메서드가 존재한다고 가정합니다.
        if (!userEntity.isApproved()) {
            // 새로 정의한 Custom 예외를 던집니다.
            throw new UserNotApprovedAuthenticationException("회원가입 승인이 아직 완료되지 않았습니다. 관리자에게 문의하세요.");
        }

        // Case 3: 정상적인 사용자 (로그인 성공)
        return userEntity;

        }
}