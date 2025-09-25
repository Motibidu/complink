package com.pcgear.complink.pcgear.KJG.user.service; // 또는 com.pcgear.complink.pcgear.KJG.user.security;

// import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.oauth2.core.user.OAuth2User; // 1. import
// 추가

// import java.util.ArrayList;
// import java.util.Collection;
// import java.util.Map;

// // ▼▼▼ 2. OAuth2User 인터페이스를 구현(implements)하도록 추가 ▼▼▼
// public class MyUserDetail implements UserDetails, OAuth2User {

// private final UserEntity userEntity;
// private Map<String, Object> attributes;

// public MyUserDetail(UserEntity userEntity) {
// this.userEntity = userEntity;
// }

// public MyUserDetail(UserEntity userEntity, Map<String, Object> attributes) {
// this.userEntity = userEntity;
// this.attributes = attributes;
// }

// @Override
// public Collection<? extends GrantedAuthority> getAuthorities() {
// Collection<GrantedAuthority> authorities = new ArrayList<>();
// return authorities;
// }

// @Override
// public String getPassword() {
// return userEntity.getPassword();
// }

// @Override
// public String getUsername() {
// return userEntity.getUsername();
// }

// @Override
// public boolean isAccountNonExpired() {
// return true;
// }

// @Override
// public boolean isAccountNonLocked() {
// return true;
// }

// @Override
// public boolean isCredentialsNonExpired() {
// return true;
// }

// @Override
// public boolean isEnabled() {
// return true;
// }

// @Override
// public Map<String, Object> getAttributes() {
// // CustomOAuth2UserService에서 받아온 추가 정보(이름, 프로필 사진 등)를 반환합니다.
// return attributes;
// }

// @Override
// public String getName() {
// return userEntity.getUsername();
// }
// }