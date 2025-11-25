package com.pcgear.complink.pcgear.User.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.pcgear.complink.pcgear.User.dto.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "user")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, length = 50, nullable = false)
	private String email;

	@Column(unique = true, nullable = false)
	private String username;

	private String password;

	@Column(nullable = false)
	private String name;

	private String tel;

	//private String address;

	// private String billingKey;

	// @Enumerated(EnumType.STRING)
	// private SubscriptionStatus subscriptionStatus;

	@Enumerated(EnumType.STRING) // Enum 타입을 DB에 String으로 저장
	@Column(nullable = false)
	private UserRole role;

	private boolean isActive;

	@Builder
	public UserEntity(String email, String username, String password, String name, String tel, String address,
			UserRole role) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.name = name;
		this.tel = tel;
		//this.address = address;
		this.role = role;
		this.isActive = true;
	}

	public void Memberupdate(String email, String name) {
		this.email = email;
		this.name = name;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(this.role.getKey()));
	}

	// Spring security가 isEnalbed()가 true일 때만 로그인을 허용함
	@Override
	public boolean isEnabled() {
		return this.isActive;
	}
}
