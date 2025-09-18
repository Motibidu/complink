package com.pcgear.complink.pcgear.KJG.user.controller;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.service.MailService;
import com.pcgear.complink.pcgear.KJG.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/find-password")
public class FindPasswordController {

    private final UserService userService;
    private final MailService mailService;

    @PostMapping("/send-mail")
    public ResponseEntity<?> sendVerificationMail(@RequestBody Map<String, String> requestMap) {
        String username = requestMap.get("username");
        String email = requestMap.get("email");

        // ★★★★★ 가장 먼저 실행되는 로그. 이 로그가 찍히는지 확인하세요. ★★★★★
        log.info("비밀번호 찾기 API '/send-mail' 호출됨. 전달된 아이디: {}, 이메일: {}", username, email);

        UserEntity user = userService.findByUsernameAndEmail(username, email);
        if (user == null) {
            // 이 조건에 걸리면 메일 발송 없이 여기서 종료됩니다.
            log.warn("일치하는 사용자 정보 없음. 아이디: {}, 이메일: {}", username, email);
            return ResponseEntity.status(404).body(Map.of("message", "입력하신 정보와 일치하는 사용자가 없습니다."));
        }

        try {
            mailService.sendVerificationMail(email);
            return ResponseEntity.ok(Map.of("message", "인증 메일이 발송되었습니다. 이메일을 확인해주세요."));
        } catch (Exception e) {
            log.error("메일 발송 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "메일 발송 중 오류가 발생했습니다."));
        }
    }

    // ... resetPassword 메서드는 그대로 ...
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestMap) {
        String username = requestMap.get("username");
        String email = requestMap.get("email");
        String code = requestMap.get("code");

        boolean isVerified = mailService.verifyCode(email, code);
        if (!isVerified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 올바르지 않습니다."));
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        userService.changePassword(username, tempPassword);
        mailService.sendTemporaryPasswordMail(email, tempPassword);

        return ResponseEntity.ok(Map.of("message", "새로운 임시 비밀번호가 이메일로 발송되었습니다."));
    }
}