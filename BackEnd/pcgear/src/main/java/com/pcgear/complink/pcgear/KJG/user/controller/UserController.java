package com.pcgear.complink.pcgear.KJG.user.controller;

import com.pcgear.complink.pcgear.KJG.user.dto.SignRequestDto;
import com.pcgear.complink.pcgear.KJG.user.service.RecaptchaService;
import com.pcgear.complink.pcgear.KJG.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "사용자 관리", description = "사용자 등록 및 로그인 상태 확인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RecaptchaService recaptchaService;

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록하고 reCAPTCHA를 검증합니다.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignRequestDto signRequestDto) {

        // reCAPTCHA 검증
        String recaptchaToken = signRequestDto.getRecaptchaToken();
        boolean isRecaptchaValid = recaptchaService.verifyRecaptcha(recaptchaToken);
        if (!isRecaptchaValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "reCAPTCHA 검증에 실패했습니다."));
        }

        // reCAPTCHA 검증 성공 시, 기존 회원가입 로직 진행
        Long userId = userService.createUser(signRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", userId, "message", "회원가입이 성공적으로 완료되었습니다."));
    }

    @Operation(summary = "로그인 상태 확인", description = "현재 사용자가 로그인되어 있는지 여부를 확인합니다.")
    @GetMapping("/isLoggedIn")
    public ResponseEntity<Map<String, Boolean>> isLoggedIn(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", true));
        } else {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false));
        }
    }
}