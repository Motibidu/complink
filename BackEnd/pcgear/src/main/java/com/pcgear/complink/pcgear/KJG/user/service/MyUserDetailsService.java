package com.pcgear.complink.pcgear.KJG.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

                UserEntity userEntity = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

                // UserEntity가 UserDetails를 구현했으므로, 직접 userEntity를 반환합니다.
                // Spring Security는 이 UserEntity의 getUsername(), getPassword(), getAuthorities()
                // 등을 사용합니다.
                return userEntity;

        }
}