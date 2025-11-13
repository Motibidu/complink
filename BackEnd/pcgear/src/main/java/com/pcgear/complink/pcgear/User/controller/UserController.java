package com.pcgear.complink.pcgear.User.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.pcgear.complink.pcgear.User.dto.SignRequestDto;
import com.pcgear.complink.pcgear.User.dto.SignupRespDto;
import com.pcgear.complink.pcgear.User.entity.UserRole;
import com.pcgear.complink.pcgear.User.service.RecaptchaService;
import com.pcgear.complink.pcgear.User.service.UserService;

import java.util.Map;

@Tag(name = "사용자 관리", description = "사용자 등록 및 로그인 상태 확인 API")
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RecaptchaService recaptchaService;

    @Operation(summary = "회원 가입", description = "reCAPTCHA 검증에 성공하면 회원가입을 진행합니다.")
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

    @GetMapping("/userId")
    public ResponseEntity<String> userId(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok().body(userDetails.getUsername());
    }

    @GetMapping("/signup-req")
    public ResponseEntity<Page<SignupRespDto>> readSignupReq(
            // 💡 @PageableDefault로 기본값 설정 (페이지 0, 사이즈 10)
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<SignupRespDto> signupPage = userService.readSignupReq(pageable);
        return ResponseEntity.ok(signupPage);
    }

    @GetMapping("/userRole")
    public ResponseEntity<String> getUserRole(@AuthenticationPrincipal UserDetails userDetails) {

        // String authorityString =
        // userDetails.getAuthorities().iterator().next().getAuthority();

        // String roleKey = authorityString.replaceFirst("ROLE_", "");
        // log.info("roleKey: {}", roleKey);

        // try {
        // UserRole role = UserRole.valueOf(roleKey);
        // return ResponseEntity.ok(role);
        // } catch (IllegalArgumentException e) {
        // System.err.println("ERROR: Undefined UserRole value from authority: " +
        // roleKey + " | " + e.getMessage());
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        // }
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // "ROLE_USER", "ROLE_SUBSCRIBER"
                .filter(authString -> authString.equals("ROLE_ADMIN") || authString.equals("ROLE_USER"))
                .findFirst()
                .orElse("ROLE_USER"); // 기본값

        // "ROLE_" 접두어 제거 (예: "USER" 또는 "ADMIN")
        String roleKey = role.replace("ROLE_", "");
        log.info("roleKey: {}", roleKey); // "USER" 또는 "ADMIN"

        return ResponseEntity.ok(roleKey);
    }

    @PostMapping("/signup-approve/{email}")
    public ResponseEntity<String> signupApprove(@PathVariable(name = "email") String email) {
        userService.signupApprove(email);

        return ResponseEntity.ok("회원가입 승인이 완료되었습니다.");
    }

    @DeleteMapping("/signup-reject/{email}")
    public ResponseEntity<String> signupReject(@PathVariable(name = "email") String email) {
        userService.signupReject(email);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}