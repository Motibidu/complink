package com.pcgear.complink.pcgear.Payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Payment.exception.PaymentVerificationException;
import com.pcgear.complink.pcgear.Payment.model.AccessTokenResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;
import com.pcgear.complink.pcgear.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import io.portone.sdk.server.webhook.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    @Lazy
    @Autowired
    private PaymentService self;

    private final PortoneProperties portoneProperties;

    private final SubscriptionService subscriptionService;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    private final PaymentLinkService paymentLinkService;
    private final RestClient restClient;

    // 빌링키 기반 단건(즉시) 결제 실행
    public Payment executeImmediatePayment(UserEntity user, SubscriptionRequest subscriptionRequest) {
        log.info("단건(즉시) 결제 요청 시작 ==========================================");

        final String paymentId = "payment-" + UUID.randomUUID().toString();
        final String uri = portoneProperties.getApiUrl() + "/payments/" + paymentId + "/billing-key";

        Map<String, Object> requestBody = Map.of(
                "billingKey", subscriptionRequest.getBillingKey(),
                "orderName", "즉시결제",
                "customer", createCustomerMap(user),
                "amount", Map.of("total", subscriptionRequest.getAmount()),
                "currency", "KRW");

        try {
            Map<String, Object> responseMap = restClient.post()
                    .uri(uri)
                    .header("Authorization", "PortOne " + portoneProperties.getApiSecret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new RuntimeException("결제 요청 실패(4xx): " + new String(res.getBody().readAllBytes()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new RuntimeException("결제 요청 실패(5xx): " + new String(res.getBody().readAllBytes()));
                    })
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            return savePaymentFromResponse(responseMap, paymentId, user, subscriptionRequest);

        } catch (Exception e) {
            log.error("단건 결제 처리 중 오류 발생", e);
            throw new RuntimeException("단건 결제 실패", e);
        }
    }

    private Payment savePaymentFromResponse(Map<String, Object> responseMap, String paymentId, UserEntity user,
            SubscriptionRequest request) {
        Map<String, Object> paymentDetail = (Map<String, Object>) responseMap.get("payment");
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .userId(user.getUsername())
                .amount(request.getAmount())
                .paymentMethod(request.getBillingKeyMethod())
                .paymentStatus(PaymentStatus.READY)
                .paidAt(parsePaidAt((String) paymentDetail.get("paidAt")))
                .build();
        return paymentRepository.save(payment);
    }

    private LocalDateTime parsePaidAt(String paidAtString) {
        if (paidAtString == null)
            return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.parse(paidAtString), ZoneId.systemDefault());
    }

    private Map<String, Object> createCustomerMap(UserEntity user) {
        return Map.of(
                "id", user.getUsername().toString(),
                "name", Map.of("full", user.getName()),
                "email", user.getEmail());
    }

    // 포트원 V2 웹훅 서명 검증 + 처리
    public void webhookVerify(String payload, String webhookId, String webhookSignature, String webhookTimestamp)
            throws WebhookVerificationException {
        log.info("웹훅 처리 시작 ==========================================");

        // 1. 서명 검증
        verifyWebhookSignature(payload, webhookId, webhookSignature, webhookTimestamp);

        // 2. 페이로드 파싱 (한 번만 파싱하여 type과 paymentId 모두 추출)
        Map<String, Object> webhookData = parsePayload(payload);
        if (!"Transaction.Paid".equals(webhookData.get("type"))) {
            log.info("결제 완료(Paid) 이벤트가 아니므로 무시합니다.");
            return;
        }

        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        String paymentId = (String) data.get("paymentId");
        if (paymentId == null) {
            throw new RuntimeException("페이로드에서 paymentId를 찾을 수 없습니다.");
        }
        log.info("추출된 paymentId: {}", paymentId);

        try {
            // 3. [외부 API] 결제 상세 내역 조회
            Map<String, Object> paymentDetail = getPaymentDetailFromPortOne(paymentId);
            log.info("포트원 결제 상세 조회 완료");

            // 4. [내부 DB] 트랜잭션 진입
            self.processWebhookTransaction(paymentId, paymentDetail);

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생", e);
            throw new RuntimeException("웹훅 처리 실패", e);
        }
    }

    private void verifyWebhookSignature(String payload, String id, String sig, String ts)
            throws WebhookVerificationException {
        WebhookVerifier verifier = new WebhookVerifier(portoneProperties.getWebhookSecret());
        try {
            verifier.verify(payload, id, sig, ts);
        } catch (WebhookVerificationException e) {
            throw new WebhookVerificationException(ts, e);
        }
    }

    private Map<String, Object> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    private Map<String, Object> getPaymentDetailFromPortOne(String paymentId) {
        return restClient.get()
                .uri(portoneProperties.getApiUrl() + "/payments/" + paymentId)
                .header("Authorization", "PortOne " + portoneProperties.getApiSecret())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    // 결제 유형 분기 (단건 vs 구독)
    @Transactional
    public void processWebhookTransaction(String paymentId, Map<String, Object> paymentDetail) {
        Optional<Payment> existingPaymentOpt = paymentRepository.findByPaymentId(paymentId);

        if (existingPaymentOpt.isPresent()) {
            log.info(">>> [TYPE: 단건/주문 결제] 처리");
            processOneTimePayment(existingPaymentOpt.get(), paymentDetail);
        } else {
            log.info(">>> [TYPE: 정기/구독 결제] 처리");
            processSubscriptionPayment(paymentDetail);
        }
    }

    // 단건 결제 금액 검증 + DB 업데이트
    private void processOneTimePayment(Payment dbPayment, Map<String, Object> paymentDetail) {
        Integer apiAmount = (Integer) ((Map<String, Object>) paymentDetail.get("amount")).get("total");

        if (!apiAmount.equals(dbPayment.getAmount())) {
            throw new PaymentVerificationException("결제 금액 불일치 (위변조 의심)");
        }

        String apiStatus = (String) paymentDetail.get("status");
        if ("PAID".equalsIgnoreCase(apiStatus) && dbPayment.getPaymentStatus() != PaymentStatus.PAID) {
            dbPayment.setPaymentStatus(PaymentStatus.PAID);
            dbPayment.setPaidAt(parsePaidAt((String) paymentDetail.get("paidAt")));
            paymentRepository.save(dbPayment);

            if (dbPayment.getOrder() != null) {
                paymentLinkService.finalizeOrderPayment(dbPayment.getOrder());
            }
        }
    }

    private void processSubscriptionPayment(Map<String, Object> paymentDetail) {
        subscriptionService.processSubscriptionPayment(paymentDetail);
    }
}
