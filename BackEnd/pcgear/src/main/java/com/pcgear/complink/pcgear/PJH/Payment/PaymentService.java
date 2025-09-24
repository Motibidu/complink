package com.pcgear.complink.pcgear.PJH.Payment; // 실제 프로젝트의 패키지 경로로 수정하세요.

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// --- 필요한 클래스 Import ---
import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;

import io.portone.sdk.server.webhook.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookVerifier;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    // @Value("${portone.api.secret}")
    private String portoneApiSecret = "FkLCYZzsKhVsoZxz8aZEWXTiRsRYisWO9CBuzCUuooCjBU78TCMCEmdt3NydMvlG63zysLVjQMLAsdA1";

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(UserRepository userRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }
    // private final PaymentScheduleRepository paymentScheduleRepository; // 결제 예약
    // 정보 저장을 위한 Repository

    /**
     * 프론트엔드로부터 빌링키를 받아 첫 결제 및 다음 회차 예약을 처리합니다.
     * 
     * @param request 프론트엔드에서 받은 billingKey와 주문 정보
     * @param userId  현재 로그인된 사용자의 ID
     */
    @Transactional
    public void processSubscription(SubscriptionRequest request, String userId) {
        UserEntity user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        if (user.getBillingKey() == null) {
            user.setBillingKey(request.getBillingKey());
            userRepository.save(user);
            log.info("사용자 ID {}의 빌링키 저장 완료.", userId);
        }

        executeImmediatePayment(user, request);
        // scheduleNextPayment(user, request, 5);
    }

    private void executeImmediatePayment(UserEntity user, SubscriptionRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String paymentId = "payment-" + UUID.randomUUID().toString();
        String url = "https://api.portone.io/payments/" + paymentId + "/billing-key";

        Map<String, Object> requestBody = Map.of(
                "billingKey", request.getBillingKey(),
                "orderName", request.getOrderName(),
                "customer", createCustomerMap(user),
                "amount", Map.of("total", request.getAmount()),
                "currency", "KRW");

        HttpEntity<Map<String, Object>> entity = createHttpEntity(requestBody);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("response: " + response);
            if (response.getStatusCode() == HttpStatus.OK) {

                ObjectMapper objectMapper = new ObjectMapper();
                // 2. 응답 Body(JSON 문자열)를 Map으로 변환
                Map<String, Object> responseBodyMap = objectMapper.readValue(response.getBody(), new TypeReference<>() {
                });

                // 3. Map에서 'paidAt' 값을 추출
                Map<String, Object> paymentInfo = (Map<String, Object>) responseBodyMap.get("payment");
                String paidAtString = (String) paymentInfo.get("paidAt");

                // 4. 'Z'로 끝나는 UTC 시간을 Instant로 파싱 후, 시스템 기본 시간대의 LocalDateTime으로 변환
                Instant instant = Instant.parse(paidAtString);
                LocalDateTime paidAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                Payment payment = new Payment();
                payment.setPaymentId(paymentId);
                payment.setUserId(user.getUsername());
                payment.setAmount(request.getAmount());
                payment.setPaymentMethod("EASY_PAY");
                payment.setStatus("PAID");
                payment.setPaidAt(paidAt);
                paymentRepository.save(payment);

                log.info("사용자 ID {}의 결제가 성공적으로 완료되었습니다. (결제ID: {})", user.getUsername(), paymentId);
            }
        } catch (HttpClientErrorException e) {
            log.error("즉시 결제 실패 (사용자 ID: {}): 상태코드 - {}, 응답 - {}", user.getUsername(), e.getStatusCode(),
                    e.getResponseBodyAsString());
            // 즉시 결제가 실패하면 다음 로직으로 넘어가지 않고 예외를 던져 트랜잭션을 롤백합니다.
            throw new RuntimeException("첫 결제에 실패했습니다.");
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 다음 정기결제를 예약하는 private 메서드
     */
    private void scheduleNextPayment(UserEntity user, SubscriptionRequest request, int seconds) {
        RestTemplate restTemplate = new RestTemplate();
        String paymentId = "payment-" + UUID.randomUUID().toString();
        String url = "https://api.portone.io/payments/" + paymentId + "/schedule";

        // 다음 결제일 설정 (예: 한 달 뒤)
        // LocalDateTime nextPaymentTime = LocalDateTime.now().plusMonths(1);

        LocalDateTime nextPaymentTime = LocalDateTime.now().plusSeconds(seconds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        // 2. 서울 시간 기준으로 ZonedDateTime 생성 후 포맷 적용
        String formattedNextPaymentTime = nextPaymentTime.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
        // --- 👆 날짜 포맷 수정된 부분 ---

        Map<String, Object> paymentData = Map.of(
                "billingKey", request.getBillingKey(),
                "orderName", request.getOrderName() + " (정기결제)",
                "customer", createCustomerMap(user),
                "amount", Map.of("total", request.getAmount()),
                "currency", "KRW");
        Map<String, Object> requestBody = Map.of(
                "payment", paymentData,
                "timeToPay", formattedNextPaymentTime);

        HttpEntity<Map<String, Object>> entity = createHttpEntity(requestBody);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("사용자 ID {}의 다음 정기결제가 성공적으로 예약되었습니다. (결제ID: {})", user.getUsername(), paymentId);
                Subscription subscription = new Subscription();
                subscription.setUserId(user.getUsername());
                subscription.setBillingKey(request.getBillingKey());
                subscription.setStatus("ACTIVE");
                subscription.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
                subscription.setNextBillingTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMonths(1));
            }
        } catch (HttpClientErrorException e) {
            log.error("정기결제 예약 실패 (사용자 ID: {}): 상태코드 - {}, 응답 - {}", user.getUsername(), e.getStatusCode(),
                    e.getResponseBodyAsString());
            // TODO: 예약 실패 시 관리자에게 알림을 보내는 등 예외 처리 로직 추가
        }
    }

    // HttpEntity 생성을 위한 헬퍼 메서드
    private HttpEntity<Map<String, Object>> createHttpEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + portoneApiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    // Customer 정보 Map 생성을 위한 헬퍼 메서드
    private Map<String, Object> createCustomerMap(UserEntity user) {
        return Map.of(
                "id", user.getUsername().toString(),
                "name", Map.of("full", user.getName()),
                "email", user.getEmail());
    }

    public void processWebhook(String payload, String webhookId, String webhookSignature, String webhookTimestamp,
            @AuthenticationPrincipal UserDetails userDetails) {
        WebhookVerifier verifier = new WebhookVerifier(portoneApiSecret);
        try {
            verifier.verify(payload, webhookId, webhookSignature, webhookTimestamp);
        } catch (WebhookVerificationException e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String paymentId = null;
        try {
            // 2. webhookTimestamp에 담겨온 JSON 문자열을 Map<String, Object> 타입으로 변환
            Map<String, Object> payloadMap = objectMapper.readValue(webhookTimestamp, new TypeReference<>() {
            });

            // 3. 중첩된 구조에서 'data' 객체를 먼저 추출
            Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
            log.info("추출된 data 객체: {}", data);

            // 4. 'data' 객체 안에서 'paymentId'를 최종적으로 추출
            paymentId = (String) data.get("paymentId");
            log.info("✅ 최종 추출된 paymentId: {}", paymentId);

        } catch (JsonProcessingException e) {
            log.error("웹훅 JSON 파싱 중 오류가 발생했습니다.", e);
        }

        WebClient webClient = WebClient.create();
        Map<String, Object> paymentDetail = webClient
                .get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + portoneApiSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("paymentDetail: {}", paymentDetail);

        Object billingKeyValueObject = paymentDetail.get("billingKey");
        String billingKey = null;
        if (billingKeyValueObject != null) {
            billingKey = (String) billingKeyValueObject;
        }
        UserEntity user = userRepository.findByUsername(userDetails.getUsername()).get();

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setAmount(1000);
        subscriptionRequest.setOrderName("정기결제");
        subscriptionRequest.setBillingKey(billingKey);

        scheduleNextPayment(user, subscriptionRequest, 3000);
    }
}