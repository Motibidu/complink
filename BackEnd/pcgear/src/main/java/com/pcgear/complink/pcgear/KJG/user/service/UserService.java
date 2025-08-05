package com.pcgear.complink.pcgear.KJG.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcgear.complink.pcgear.KJG.user.dto.LoginResponseDto;
import com.pcgear.complink.pcgear.KJG.user.dto.SignRequestDto;
import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	//C : 회원가입
	@Transactional
	public Long createUser(SignRequestDto signRequestDto) {
		String password1 = signRequestDto.getPassword();
		String password2 = signRequestDto.getPasswordConfirm();
		
		if(!password1.equals(password2)) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}
		String encodedPassword = passwordEncoder.encode(signRequestDto.getPassword());
		
		UserEntity userEntity = signRequestDto.toEntity(encodedPassword);
		
		UserEntity saveEntity = userRepository.save(userEntity);
		
		
		return saveEntity.getId();
	}
	
	//R : 마이페이지 확인용
	@Transactional(readOnly = true)
	public LoginResponseDto MyUser(Long id) {
	
		UserEntity userEntity = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을수 없습니다 " + id));	
		
		return new LoginResponseDto(userEntity);
	}
	
	
	//U : 이메일 변경, 이름 변경
	@Transactional
	public Long UpdateUser(LoginResponseDto loginResponseDto) {
		UserEntity userEntity = userRepository.findById(loginResponseDto.getId())
				.orElseThrow(() -> new IllegalArgumentException("ID값이 존재 하지 않습니다."));
		
		userEntity.Memberupdate(loginResponseDto.getEmail(), loginResponseDto.getUsername());
		
		userRepository.save(userEntity);
				
				return userEntity.getId();
	}
	
	//D : 회원탈퇴
	@Transactional
	public void DeleteUser(Long id) {
		
		UserEntity userEntity = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다 : " +id));
		
		userRepository.delete(userEntity);
	}
	
	@Transactional(readOnly = true)
    public String findUserIdByEmail(String email) {
        // userRepository의 findByEmail은 Optional<UserEntity>를 반환합니다.
        return userRepository.findByEmail(email)
                .map(UserEntity::getUsername) // UserEntity에서 로그인 아이디(username)를 가져옵니다.
                .orElse(null); // 사용자가 존재하지 않으면 null을 반환합니다.
    }
	
	 @Transactional(readOnly = true)
	    public UserEntity findByUsernameAndEmail(String username, String email) {
	        return userRepository.findByUsernameAndEmail(username, email)
	                .orElse(null);
	    }


	 @Transactional
	    public void changePassword(String username, String tempPassword) {
	        // 1. 아이디로 사용자를 찾습니다.
	        UserEntity userEntity = userRepository.findByUsername(username)
	                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 사용자를 찾을 수 없습니다: " + username));
	        
	        // 2. 임시 비밀번호를 암호화합니다.
	        String encodedPassword = passwordEncoder.encode(tempPassword);
	        
	        // 3. UserEntity의 비밀번호를 업데이트합니다.
	        //    (주의: UserEntity에 password 필드를 업데이트하는 메서드가 있어야 합니다. 예: setPassword)
	        userEntity.updatePassword(encodedPassword); 
	    }
}

