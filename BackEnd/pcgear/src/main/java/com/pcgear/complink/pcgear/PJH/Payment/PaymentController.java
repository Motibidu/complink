package com.pcgear.complink.pcgear.PJH.Payment; // 실제 프로젝트의 패키지 경로로 수정하세요.

// --- 필요한 클래스 Import ---
import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment") // 이 컨트롤러의 모든 API는 /api/payment로 시작합니다.
public class PaymentController {

    @Value("${portone.webhook.secret}")
    private String portoneApiSecret;
    private final PaymentService paymentService;
    private final UserRepository userRepository; // 사용자 ID를 조회하기 위해 필요합니다.

    /**
     * 프론트엔드로부터 빌링키와 주문 정보를 받아 구독 처리를 시작하는 API 엔드포인트입니다.
     * 
     * @param request     프론트엔드에서 보낸 요청 DTO (billingKey, orderName, amount 포함)
     * @param userDetails Spring Security가 주입해주는 현재 로그인된 사용자의 정보
     * @return 성공 메시지를 포함한 ResponseEntity
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Spring Security의 UserDetails에서 사용자 이름(username)을 가져옵니다.
        String username = userDetails.getUsername();

        // 2. 사용자 이름으로 DB에서 User 엔티티를 조회하여 고유 ID(Long 타입)를 얻습니다.
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));
        String userId = user.getUsername();

        // 3. 조회한 userId와 요청 데이터를 서비스 계층으로 전달합니다.
        paymentService.processSubscription(request, userId);

        // 4. 모든 처리가 성공적으로 완료되면 200 OK 응답과 함께 성공 메시지를 반환합니다.
        return ResponseEntity.ok(Map.of("message", "구독 처리가 성공적으로 시작되었습니다."));
    }

    @PostMapping("/portone-webhook")
    public ResponseEntity<String> portOneWebhook(
            @RequestBody String payload, // 1. 원본 Body를 String으로 받습니다.
            @RequestHeader("webhook-id") String webhookId, // 2. 헤더 값을 받아옵니다.
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp) {
        log.info("포트원 웹훅 원본(Raw) 데이터 수신: {}", payload);

        try {
            // 실제 비즈니스 로직은 서비스 계층에 위임합니다.
            //
            paymentService.processWebhook(webhookId, webhookSignature, webhookTimestamp, payload);

            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: {}", e.getMessage());
            // 문제가 발생했더라도, 포트원의 재전송을 막기 위해 200 OK를 보내는 것을 고려할 수 있습니다.
            // 또는 에러를 반환하여 재전송을 유도할 수도 있습니다. (포트원 정책 확인 필요)
            return ResponseEntity.badRequest().body("Error processing webhook.");

        }

    }
}
