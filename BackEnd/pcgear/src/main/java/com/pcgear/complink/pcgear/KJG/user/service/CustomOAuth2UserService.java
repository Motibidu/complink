package com.pcgear.complink.pcgear.KJG.user.service;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Override
    // public OAuth2User loadUser(OAuth2UserRequest userRequest) throws
    // OAuth2AuthenticationException {
    // // 1. 구글로부터 사용자 정보를 가져옵니다.
    // OAuth2User oAuth2User = super.loadUser(userRequest);
    // Map<String, Object> attributes = oAuth2User.getAttributes();

    // String email = (String) attributes.get("email");
    // String name = (String) attributes.get("name");

    // // 2. 우리 DB에 해당 이메일의 사용자가 있는지 확인합니다.
    // Optional<UserEntity> userOptional = userRepository.findByEmail(email);

    // UserEntity userEntity;
    // if (userOptional.isPresent()) {
    // // 3-1. 이미 가입된 회원이면, 그 정보를 그대로 사용합니다.
    // userEntity = userOptional.get();
    // } else {
    // // 3-2. 가입되지 않은 회원이면, 정보를 바탕으로 자동 회원가입을 진행합니다.
    // userEntity = UserEntity.builder()
    // .email(email)
    // .username(email) // 아이디를 이메일로 자동 설정
    // .name(name)
    // // 소셜 로그인 사용자는 비밀번호가 필요 없으므로, 임의의 값을 암호화하여 저장
    // .password(passwordEncoder.encode(UUID.randomUUID().toString()))
    // .tel("010-0000-0000") // 필수값이므로 임의의 기본값 설정
    // .address("기본 주소") // 필수값이므로 임의의 기본값 설정
    // .build();
    // userRepository.save(userEntity);
    // }

    // // 4. 시큐리티 세션에 저장할 UserDetails 객체를 반환합니다.
    // // MyUserDetail 클래스에 OAuth2 속성을 받는 생성자가 필요할 수 있습니다.
    // return new MyUserDetail(userEntity, attributes);
    // }
}