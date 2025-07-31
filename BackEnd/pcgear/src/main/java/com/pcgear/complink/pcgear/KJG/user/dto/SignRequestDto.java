package com.pcgear.complink.pcgear.KJG.user.dto;


import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignRequestDto {

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
			message = "이메일 형식이 아닙니다.")
	private String email;
	
	@NotBlank(message = "아이디를 입력해 주세요.")
	@Size(min = 8, max = 12,message = "아이디를 8자이상 12자이하로 입력해주세요.")
	private String username;
	
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$"
			, message = "비밀번호는 영문, 숫자 , 특수문자가 포함되어야 합니다.")
	private String password;
	
	@NotBlank(message = "비밀번호를 다시 한번 입력해 주세요.")
	private String passwordConfirm;
	
	@NotBlank(message = "성함을 입력해 주세요.")
	private String name;
	
	@NotBlank(message = "전화번호를 입력해 주세요.")
	private String tel;
	
	@NotBlank(message = "주소를 입력해 주세요.")
	private String address;
	
	
	
	
	public UserEntity toEntity(String encodedPassword) {
		return UserEntity.builder()
			.email(email)
			.username(username)
			.password(encodedPassword)
			.name(name)
			.tel(tel)
			.address(address)
			.build();
	}
	
	
}
