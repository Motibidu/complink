package com.pcgear.complink.pcgear.KJG.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true, length = 50, nullable = false)
	private String email;
	
	@Column(unique = true, nullable = false)
	private String username;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String tel;
	
	@Column(nullable = false)
	private String address;
	
	
	@Builder
	public UserEntity(String email, String username, String password,String name, String tel, String address) {
		
		this.email = email;
		this.username = username;
		this.password = password;
		this.name = name;
		this.tel = tel;
		this.address = address;
	}
	
	
	public void Memberupdate(String email, String name) {
		this.email = email;
		this.name = name;
	}
}
