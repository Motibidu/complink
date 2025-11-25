package com.pcgear.complink.pcgear.User.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.User.dto.SubscriptionStatus;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;
import com.pcgear.complink.pcgear.exception.UserNotApprovedAuthenticationException;
import com.pcgear.complink.pcgear.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UserNotFoundException {

                UserEntity userEntity = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

                // 승인 대기중이면 예외 반환
                if (!userEntity.isActive()) {
                        throw new UserNotApprovedAuthenticationException("회원가입 승인이 아직 완료되지 않았습니다. 관리자에게 문의하세요.");
                }

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()));
                // if (userEntity.getSubscriptionStatus() == SubscriptionStatus.ACTIVE) {
                //         authorities.add(new SimpleGrantedAuthority("ROLE_SUBSCRIBER"));
                // }

                return new org.springframework.security.core.userdetails.User(
                                userEntity.getUsername(),
                                userEntity.getPassword(),
                                userEntity.isEnabled(), // enabled (계정 활성화 여부)
                                true, // accountNonExpired
                                true, // credentialsNonExpired
                                true, // accountNonLocked
                                authorities // ⬅️ [수정됨] 최종 권한 목록
                );

        }
}