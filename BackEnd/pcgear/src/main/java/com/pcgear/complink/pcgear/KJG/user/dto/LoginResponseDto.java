package com.pcgear.complink.pcgear.KJG.user.dto;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponseDto {

	private final Long id;

	private final String email;

	private final String username;

	private final String password;

	private final String name;

	private final String tel;

	private final String address;

	@Builder
	public LoginResponseDto(UserEntity userEntity) {

		this.id = userEntity.getId();
		this.email = userEntity.getEmail();
		this.username = userEntity.getUsername();
		this.password = userEntity.getPassword();
		this.name = userEntity.getName();
		this.tel = userEntity.getTel();
		this.address = userEntity.getAddress();
	}
}
