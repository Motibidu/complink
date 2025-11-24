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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final UserRepository userRepository;


    // @PostMapping("/subscribe")
    // public ResponseEntity<String> subscribe(@RequestBody SubscriptionRequest
    // request,
    // @AuthenticationPrincipal UserDetails userDetails) {
    // String username = userDetails.getUsername();
    // UserEntity user = userRepository.findByUsername(username)
    // .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " +
    // username));

    // paymentService.executeImmediatePayment(user, request).block();

    // return ResponseEntity.ok("구독 처리가 성공적으로 시작되었습니다.");
    // }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

        // [변경] .block() 제거! (Service가 동기로 동작함)
        paymentService.executeImmediatePayment(user, request);

        return ResponseEntity.ok("구독 처리가 성공적으로 시작되었습니다.");
    }

    // @PostMapping("/webhook-verify")
    // public ResponseEntity<String> portOneWebhook(
    // @RequestBody String payload, // 1. 원본 Body를 String으로 받습니다.
    // @RequestHeader("webhook-id") String webhookId, // 2. 헤더 값을 받아옵니다.
    // @RequestHeader("webhook-signature") String webhookSignature,
    // @RequestHeader("webhook-timestamp") String webhookTimestamp) {
    // log.info("payload: {}", payload);
    // log.info("webhook-id: {}", webhookId);
    // log.info("webhook-signature: {}", webhookSignature);
    // log.info("webhook-timestamp: {}", webhookTimestamp);

    // try {
    // paymentService.webhookVerify(payload, webhookId, webhookSignature,
    // webhookTimestamp);
    // return ResponseEntity.ok("Webhook processed successfully.");
    // } catch (WebhookVerificationException e) {
    // log.error("웹훅 검증 실패로 인해 요청 거부: {}", e.getMessage());
    // return ResponseEntity.badRequest().body("Webhook verification failed: Invalid
    // signature or payload.");
    // } catch (PaymentVerificationException e) {
    // log.error("웹훅 검증 실패로 인해 요청 거부: {}", e.getMessage());
    // return ResponseEntity.badRequest().body("Webhook verification failed: " +
    // e.getMessage());
    // } catch (Exception e) {
    // log.error("웹훅 처리 중 내부 오류 발생: {}", e.getMessage());
    // return ResponseEntity.internalServerError().body("Error processing webhook
    // internally.");

    // }
    // }

    // 포트원 v2 웹훅
    @PostMapping("/webhook-verify")
    public ResponseEntity<String> portOneWebhook(
            @RequestBody String payload,
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp) {

        log.info(">>> [Webhook V2] Received - ID: {}", webhookId);

        try {
            // Service 내부에서 검증 -> 파싱 -> API 조회 -> DB 처리 순차 실행
            paymentService.webhookVerify(payload, webhookId, webhookSignature, webhookTimestamp);
            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (WebhookVerificationException e) {
            log.error("웹훅 서명 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook verification failed.");
        } catch (PaymentVerificationException e) {
            log.error("결제 검증 실패 (금액 불일치 등): {}", e.getMessage());
            // 비즈니스 로직 실패지만, 포트원 재시도를 막기 위해 200을 줄 수도 있음 (정책에 따라 결정)
            // 여기서는 요청 실패로 처리
            return ResponseEntity.badRequest().body("Payment verification failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("웹훅 처리 중 시스템 오류", e);
            return ResponseEntity.internalServerError().body("Error processing webhook.");
        }
    }

    // @PostMapping("/webhook/verify/paymentLink")
    // public ResponseEntity<String> webhookVerifyUrl(
    // @RequestBody WebhookRequest webhookRequest,
    // @AuthenticationPrincipal UserDetails userDetails) {

    // try {
    // // 서비스 호출 (내부에서 API 호출 -> DB 저장 순서로 처리됨)
    // paymentService.processPaymentLinkWebhook(webhookRequest);
    // return ResponseEntity.ok("Webhook processed successfully.");

    // } catch (Exception e) {
    // log.error("웹훅 처리 실패: {}", e.getMessage());
    // // 포트원 서버에게는 400을 리턴하여 재발송을 유도하거나 에러를 알림
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    @PostMapping("/webhook/verify/paymentLink")
    public ResponseEntity<String> webhookVerifyUrl(@RequestBody WebhookRequest webhookRequest) {

        log.info(">>> [Webhook Link] Received - ImpUid: {}", webhookRequest.getImpUid());

        try {
            // Service 호출 (API 조회 -> 검증 -> DB 트랜잭션)
            paymentService.processPaymentLinkWebhook(webhookRequest);
            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (Exception e) {
            log.error("웹훅 처리 실패: {}", e.getMessage());
            // 400/500 에러를 반환하면 포트원 서버가 일정 간격으로 재시도함
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
