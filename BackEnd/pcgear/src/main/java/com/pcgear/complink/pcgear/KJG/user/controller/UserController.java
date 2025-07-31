package com.pcgear.complink.pcgear.KJG.user.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.KJG.user.dto.LoginResponseDto;
import com.pcgear.complink.pcgear.KJG.user.dto.SignRequestDto;
import com.pcgear.complink.pcgear.KJG.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	
	//회원가입 페이지
	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody SignRequestDto signRequestDto) {
		try {
			Long userId = userService.createUser(signRequestDto);
			
			// 성공 시, 생성된 사용자 ID와 메시지를 JSON 형태로 반환합니다.
			// 상태 코드는 'Created(201)'를 사용합니다.
			return ResponseEntity.status(HttpStatus.CREATED)
			                     .body(Map.of("userId", userId, "message", "회원가입이 성공적으로 완료되었습니다."));
			                     
		} catch (IllegalArgumentException e) {
			// UserService에서 비밀번호 불일치 등 예외 발생 시,
			// 에러 메시지를 JSON 형태로 담아 400 Bad Request로 응답합니다.
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
	//마이페이지
	@GetMapping("/home/{id}")
	public ResponseEntity<LoginResponseDto> findUser(@PathVariable Long id) {
		return ResponseEntity.ok(userService.MyUser(id));
		
	}
	
	//회원정보 수정
	@PutMapping("/home/{id}/update")
	public ResponseEntity<Void> UpdateEmailAndName(@PathVariable Long id, @RequestBody LoginResponseDto loginResponseDto) {
		userService.UpdateUser(loginResponseDto);
		return ResponseEntity.ok().build();
		
	}
	
	//회원탈퇴
	@DeleteMapping("/home/{id}/delete")
	public ResponseEntity<Void> DeleteUserInfo(@PathVariable Long id) {
		userService.DeleteUser(id);
		return ResponseEntity.noContent().build();
	}

}
