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

    private final SellService sellService;
    // @Value("${portone.webhook.secret}")
    private String portoneWebHookSecret = "whsec_JLpNT1u+qOJbJ8zFwa2Ff8Fn0MAiG8HpgoJFL+ZFL1I=";
    private final PaymentService paymentService;
    private final PaymentLinkService paymentLinkService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

        paymentService.executeImmediatePayment(user, request).block();

        return ResponseEntity.ok("구독 처리가 성공적으로 시작되었습니다.");
    }

    @PostMapping("/webhook-verify")
    public ResponseEntity<String> portOneWebhook(
            @RequestBody String payload, // 1. 원본 Body를 String으로 받습니다.
            @RequestHeader("webhook-id") String webhookId, // 2. 헤더 값을 받아옵니다.
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp) {
        log.info("payload: {}", payload);
        log.info("webhook-id: {}", webhookId);
        log.info("webhook-signature: {}", webhookSignature);
        log.info("webhook-timestamp: {}", webhookTimestamp);

        try {
            paymentService.webhookVerify(payload, webhookId, webhookSignature, webhookTimestamp);
            return ResponseEntity.ok("Webhook processed successfully.");
        } catch (WebhookVerificationException e) {
            log.error("웹훅 검증 실패로 인해 요청 거부: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook verification failed: Invalid signature or payload.");
        } catch (PaymentVerificationException e) {
            log.error("웹훅 검증 실패로 인해 요청 거부: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook verification failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("웹훅 처리 중 내부 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing webhook internally.");

        }

    }

    @PostMapping("/webhook/verify/paymentLink")
    public ResponseEntity<String> webhookVerifyUrl(
            @RequestBody WebhookRequest webhookRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("webhookRequest: {}", webhookRequest);

        try {
            // 1. 액세스토큰 발급
            String accessToken = paymentService.getAccessToken();
            log.info("accessToken: {}", accessToken);

            // 2. impUid를 이용, 단건 결제내역 포트원 조회
            SingleInquiryResponse.ResponseData singleInquiryResponseData = paymentService.getSingleInquiry(
                    webhookRequest.getImpUid(),
                    accessToken).getResponse();
            log.info("singleInquiryResponseData: {}", singleInquiryResponseData);

            // 3. merchantUid를 이용, 주문 내역 db조회
            Order order = orderRepository.findByMerchantUid(webhookRequest.getMerchantUid())
                    .orElseThrow(() -> {
                        log.error("Order not found for merchant_uid: " + webhookRequest.getMerchantUid());
                        return new EntityNotFoundException("주문을 찾을 수 없습니다.");
                    });
            log.info("order found by merchantUid: {}", order);

            // 4. 결제 금액 비교
            BigDecimal amountToBePaid = order.getGrandAmount(); // DB에 저장된 실제 주문 금액
            BigDecimal paidAmount = singleInquiryResponseData.getAmount(); // 포트원으로부터 조회한 결제 금액
            String paymentStatus = singleInquiryResponseData.getStatus(); // 포트원으로부터 조회한 결제 상태

            if (paidAmount.compareTo(amountToBePaid) != 0) {
                log.error("Forgery attempt detected: merchant_uid " + webhookRequest.getMerchantUid() +
                        ", Expected: " + amountToBePaid + ", Paid: " + paidAmount);
                throw new RuntimeException("결제 금액 불일치 (위조된 결제 시도)");
            }

            order.setImpUid(webhookRequest.getImpUid());

            switch (paymentStatus) {
                case "paid": // 결제 완료
                    paymentService.finalizeOrderPayment(order);

                    log.info("Payment completed for order {}", webhookRequest.getMerchantUid());
                    break;
                case "cancelled": // 결제 취소
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    log.info("Payment cancelled for order {}", webhookRequest.getMerchantUid());
                    // TODO: 취소 처리 로직
                    break;
                case "failed": // 결제 실패
                    order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
                    orderRepository.save(order);
                    log.info("Payment failed for order ", webhookRequest.getMerchantUid());
                    // TODO: 실패 처리 로직
                    break;
                default:
                    log.warn("Unknown payment status for order ",
                            webhookRequest.getMerchantUid() + ": " + paymentStatus);
                    order.setOrderStatus(OrderStatus.UNKNOWN_ERROR);
                    orderRepository.save(order);
                    break;
            }

            String message = "주문번호: " + order.getOrderId() + "번의 주문이 결제되었습니다. 판매조회에서 확인해주세요.";
            messagingTemplate.convertAndSend("/topic/notifications", message);

            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook.");
        }
    }
}
