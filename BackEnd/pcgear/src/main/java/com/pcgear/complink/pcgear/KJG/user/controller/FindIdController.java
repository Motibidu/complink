package com.pcgear.complink.pcgear.KJG.user.controller;

import com.pcgear.complink.pcgear.KJG.user.service.MailService;
import com.pcgear.complink.pcgear.KJG.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/find")
public class FindIdController {

    private final UserService userService;
    private final MailService mailService;

    @PostMapping("/send-mail")
    public ResponseEntity<?> sendVerificationMail(@RequestBody Map<String, String> requestMap) {
        String email = requestMap.get("email");
        log.info("아이디 찾기 요청 이메일: {}", email);

        String userId = userService.findUserIdByEmail(email);
        if (userId == null) {
            log.warn("가입되지 않은 이메일로 아이디 찾기 시도: {}", email);
            return ResponseEntity.status(404).body(Map.of("message", "가입되지 않은 이메일입니다."));
        }

        try {
            mailService.sendVerificationMail(email);
            log.info("인증 메일 발송 성공: {}", email);
            return ResponseEntity.ok(Map.of("message", "인증 메일이 성공적으로 발송되었습니다."));
        } catch (Exception e) {
            log.error("아이디 찾기 메일 발송 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "메일 발송 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCodeAndReturnId(@RequestBody Map<String, String> requestMap) {
        String email = requestMap.get("email");
        String code = requestMap.get("code");

        boolean isVerified = mailService.verifyCode(email, code);

        if (isVerified) {
            String userId = userService.findUserIdByEmail(email);


            return ResponseEntity.ok(Map.of("userId", userId));
            
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 올바르지 않습니다."));
        }
    }
}