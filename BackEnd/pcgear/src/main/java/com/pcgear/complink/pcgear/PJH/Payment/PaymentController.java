package com.pcgear.complink.pcgear.PJH.Payment; // 실제 프로젝트의 패키지 경로로 수정하세요.

// --- 필요한 클래스 Import ---
import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;
import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.PJH.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.PJH.Payment.model.SingleInquiryResponse;
import com.pcgear.complink.pcgear.PJH.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.PJH.Payment.model.WebhookRequest;
import com.pcgear.complink.pcgear.PJH.Sell.SellService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

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
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

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

    @PostMapping("/webhookVerify")
    public ResponseEntity<String> portOneWebhook(
            @RequestBody String payload, // 1. 원본 Body를 String으로 받습니다.
            @RequestHeader("webhook-id") String webhookId, // 2. 헤더 값을 받아옵니다.
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("payload: {}", payload);
        log.info("webhook-id: {}", webhookId);
        log.info("webhook-signature: {}", webhookSignature);
        log.info("ebhook-timestamp: {}", webhookTimestamp);
        log.info("userDetails: {}", userDetails);

        try {
            // 실제 비즈니스 로직은 서비스 계층에 위임합니다.
            //
            paymentService.processWebhook(webhookId, webhookSignature, webhookTimestamp, payload, userDetails);

            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: {}", e.getMessage());
            // 문제가 발생했더라도, 포트원의 재전송을 막기 위해 200 OK를 보내는 것을 고려할 수 있습니다.
            // 또는 에러를 반환하여 재전송을 유도할 수도 있습니다. (포트원 정책 확인 필요)
            return ResponseEntity.badRequest().body("Error processing webhook.");

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
                case "ready": // 가상계좌 발급
                    // order.setPaymentStatus(PaymentStatus.READY);
                    // order.setVbankNum(paymentData.getVbankNum());
                    // order.setVbankDate(paymentData.getVbankDate());
                    // order.setVbankName(paymentData.getVbankName());
                    // orderRepository.save(order);
                    // log.info("VBank issued for order " + merchantUid + ": " +
                    // paymentData.getVbankNum());

                    // TODO: 가상계좌 발급 안내 문자메시지 발송 (SMS 서비스 사용)
                    // if (smsService != null) {
                    // String smsMessage = String.format("가상계좌 발급 성공. 계좌 정보: %s %s %s",
                    // paymentData.getVbankNum(), paymentData.getVbankDate(),
                    // paymentData.getVbankName());
                    // smsService.sendSms(order.getBuyerTel(), smsMessage);
                    // }
                    break;
                case "paid": // 결제 완료
                    sellService.createSellAndUpdateToPaid(order.getOrderId());
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

    @PostMapping("/payment-link")
    public ResponseEntity<String> createPaymentLink() {
        try {
            String shortenedUrl = paymentLinkService.createPaymentLink(
                    "test_order_" + System.currentTimeMillis(),
                    1000,
                    "테스트 상품",
                    "01011112222");
            return ResponseEntity.ok(shortenedUrl);
        } catch (Exception e) {
            // 서비스에서 예외 발생 시 500 Internal Server Error와 함께 에러 메시지 반환
            return ResponseEntity.internalServerError().body("결제 링크 생성에 실패했습니다: " + e.getMessage());
        }
    }
}
