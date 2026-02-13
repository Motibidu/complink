package com.pcgear.complink.pcgear.Payment; // 실제 프로젝트의 패키지 경로로 수정하세요.

import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.Payment.exception.PaymentVerificationException;
import com.pcgear.complink.pcgear.Payment.model.SingleInquiryResponse;
import com.pcgear.complink.pcgear.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.Payment.model.WebhookRequest;
import com.pcgear.complink.pcgear.Sell.SellService;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;

import io.portone.sdk.server.webhook.WebhookVerificationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment") // 이 컨트롤러의 모든 API는 /api/payment로 시작합니다.
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentLinkService paymentLinkService;
    private final UserRepository userRepository;

    // @PostMapping("/subscribe")
    // public ResponseEntity<String> subscribe(@RequestBody SubscriptionRequest request,
    //         @AuthenticationPrincipal UserDetails userDetails) {
    //     String username = userDetails.getUsername();
    //     UserEntity user = userRepository.findByUsername(username)
    //             .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

    //     // [변경] .block() 제거! (Service가 동기로 동작함)
    //     paymentService.executeImmediatePayment(user, request);

    //     return ResponseEntity.ok("구독 처리가 성공적으로 시작되었습니다.");
    // }

    // 포트원 v2 웹훅
    // @PostMapping("/webhook-verify")
    // public ResponseEntity<String> portOneWebhook(
    //         @RequestBody String payload,
    //         @RequestHeader("webhook-id") String webhookId,
    //         @RequestHeader("webhook-signature") String webhookSignature,
    //         @RequestHeader("webhook-timestamp") String webhookTimestamp) {

    //     log.info(">>> [Webhook V2] Received - ID: {}", webhookId);

    //     try {
    //         // Service 내부에서 검증 -> 파싱 -> API 조회 -> DB 처리 순차 실행
    //         paymentService.webhookVerify(payload, webhookId, webhookSignature, webhookTimestamp);
    //         return ResponseEntity.ok("Webhook processed successfully.");

    //     } catch (WebhookVerificationException e) {
    //         log.error("웹훅 서명 검증 실패: {}", e.getMessage());
    //         return ResponseEntity.badRequest().body("Webhook verification failed.");
    //     } catch (PaymentVerificationException e) {
    //         log.error("결제 검증 실패 (금액 불일치 등): {}", e.getMessage());
    //         // 비즈니스 로직 실패지만, 포트원 재시도를 막기 위해 200을 줄 수도 있음 (정책에 따라 결정)
    //         // 여기서는 요청 실패로 처리
    //         return ResponseEntity.badRequest().body("Payment verification failed: " + e.getMessage());
    //     } catch (Exception e) {
    //         log.error("웹훅 처리 중 시스템 오류", e);
    //         return ResponseEntity.internalServerError().body("Error processing webhook.");
    //     }
    // }

    @PostMapping("/webhook/verify/paymentLink")
    public ResponseEntity<String> webhookVerifyUrl(@RequestBody WebhookRequest webhookRequest) {

        log.info(">>> [Webhook Link] Received - ImpUid: {}", webhookRequest.getImpUid());

        try {
            paymentLinkService.verifyWebhook(webhookRequest);
            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (Exception e) {
            log.error("웹훅 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
