package com.pcgear.complink.pcgear.KJG.user.controller;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.service.MailService;
import com.pcgear.complink.pcgear.KJG.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "이메일 인증", description = "이메일 기반 인증 (아이디 찾기, 비밀번호 재설정) API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/email-verifications") // 이메일 인증 자원
public class EmailVerificationController { // 컨트롤러 이름 변경 제안

        private final UserService userService;
        private final MailService mailService;

        // 아이디 찾기 - 인증 메일 요청
        // POST /email-verifications/users/ids/request
        @Operation(summary = "아이디 찾기 - 인증 메일 요청", description = "가입된 이메일 주소로 아이디 찾기 인증 코드를 발송합니다.")
        @PostMapping("/users/ids/request")
        public ResponseEntity<?> requestUserIdVerification(@RequestBody Map<String, String> requestMap) {
                String email = requestMap.get("email");
                log.info("아이디 찾기 요청 이메일: {}", email);

                String userId = userService.findUserIdByEmail(email);
                if (userId == null) {
                        log.warn("가입되지 않은 이메일로 아이디 찾기 시도: {}", email);
                        return ResponseEntity.status(404).body(Map.of("message", "가입되지 않은 이메일입니다."));
                }

                try {
                        mailService.sendVerificationMail(email); // 인증 코드 발송
                        log.info("아이디 찾기 인증 메일 발송 성공: {}", email);
                        return ResponseEntity.ok(Map.of("message", "인증 메일이 성공적으로 발송되었습니다."));
                } catch (Exception e) {
                        log.error("아이디 찾기 메일 발송 중 오류 발생: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError().body(Map.of("message", "메일 발송 중 오류가 발생했습니다."));
                }
        }

        // 아이디 찾기 - 인증 코드 검증 및 아이디 반환
        // POST /email-verifications/users/ids/verify
        @Operation(summary = "아이디 찾기 - 인증 코드 검증 및 아이디 반환", description = "이메일과 인증 코드를 확인하고, 일치하면 해당 아이디를 반환합니다.")
        @PostMapping("/users/ids/verify")
        public ResponseEntity<?> verifyUserIdCodeAndReturnId(@RequestBody Map<String, String> requestMap) {
                String email = requestMap.get("email");
                String code = requestMap.get("code");

                boolean isVerified = mailService.verifyCode(email, code);

                if (isVerified) {
                        String userId = userService.findUserIdByEmail(email);
                        return ResponseEntity.ok(Map.of("userId", userId, "message", "인증이 완료되었습니다."));
                } else {
                        return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 올바르지 않습니다."));
                }
        }

        // 비밀번호 재설정 - 인증 메일 요청
        // POST /email-verifications/users/passwords/request
        @Operation(summary = "비밀번호 재설정 - 인증 메일 요청", description = "아이디와 이메일로 사용자 확인 후, 임시 비밀번호 재설정을 위한 인증 코드를 발송합니다.")
        @PostMapping("/users/passwords/request")
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

        // 비밀번호 재설정 - 인증 코드 검증 및 비밀번호 변경
        // PUT /email-verifications/users/passwords/reset
        @Operation(summary = "비밀번호 재설정 - 인증 코드 검증 및 임시 비밀번호 발급", description = "아이디, 이메일, 인증 코드를 검증하고, 새 임시 비밀번호를 발급하여 이메일로 전송합니다.")
        @PutMapping("/users/passwords/reset") // 비밀번호는 자원 자체를 변경하므로 PUT이 적합
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
