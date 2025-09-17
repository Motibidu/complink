package com.pcgear.complink.pcgear.KJG.user.controller;

import com.pcgear.complink.pcgear.KJG.user.dto.LoginResponseDto;
import com.pcgear.complink.pcgear.KJG.user.dto.SignRequestDto;
import com.pcgear.complink.pcgear.KJG.user.service.RecaptchaService;
import com.pcgear.complink.pcgear.KJG.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RecaptchaService recaptchaService;

    // 회원가입 페이지
    @PostMapping("/signup") // React에서 "/api/register"로 요청했다면 이 부분을 "/api/register"로 맞춰야 합니다.
    public ResponseEntity<?> signup(@Valid @RequestBody SignRequestDto signRequestDto) {
        
        // --- 1. reCAPTCHA 검증 로직 추가 ---
        String recaptchaToken = signRequestDto.getRecaptchaToken();
        boolean isRecaptchaValid = recaptchaService.verifyRecaptcha(recaptchaToken);

        if (!isRecaptchaValid) {
            // 검증 실패 시, 400 Bad Request와 함께 에러 메시지를 JSON 형태로 반환합니다.
            // 프론트엔드에서는 이 메시지를 사용자에게 보여줄 수 있습니다.
            return ResponseEntity.badRequest().body(Map.of("message", "reCAPTCHA 검증에 실패했습니다."));
        }
        // --- reCAPTCHA 검증 로직 끝 ---

        // 2. reCAPTCHA 검증 성공 시, 기존 회원가입 로직 진행
        try {
            Long userId = userService.createUser(signRequestDto);

            // 성공 시, 생성된 사용자 ID와 메시지를 JSON 형태로 반환합니다.
            // 상태 코드는 'Created(201)'를 사용합니다.
            return ResponseEntity.status(HttpStatus.CREATED)
                                 .body(Map.of("userId", userId, "message", "회원가입이 성공적으로 완료되었습니다."));

        } catch (IllegalArgumentException e) {
            // UserService에서 아이디 중복, 비밀번호 불일치 등 예외 발생 시,
            // 에러 메시지를 JSON 형태로 담아 400 Bad Request로 응답합니다.
            // 프론트엔드에서 error.response.data.message로 접근할 수 있도록 키를 'message'로 통일하는 것이 좋습니다.
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 마이페이지
    @GetMapping("/home/{id}")
    public ResponseEntity<LoginResponseDto> findUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.MyUser(id));
    }

    // 회원정보 수정
    @PutMapping("/home/{id}/update")
    public ResponseEntity<Void> UpdateEmailAndName(@PathVariable Long id, @RequestBody LoginResponseDto loginResponseDto) {
        userService.UpdateUser(loginResponseDto); // id를 명시적으로 넘겨주는 것이 좋습니다.
        return ResponseEntity.ok().build();
    }

    // 회원탈퇴
    @DeleteMapping("/home/{id}/delete")
    public ResponseEntity<Void> DeleteUserInfo(@PathVariable Long id) {
        userService.DeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/login/isLoggedIn")
    public ResponseEntity<Map<String, Boolean>> isLoggedIn(@AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails != null) {
            return ResponseEntity.ok().body(Map.of(
                "isLoggedIn", true
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "isLoggedIn", false
            ));
        }
    }
}