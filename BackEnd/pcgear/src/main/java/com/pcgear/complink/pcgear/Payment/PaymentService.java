package com.pcgear.complink.pcgear.Payment; // 실제 프로젝트의 패키지 경로로 수정하세요.

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.Payment.exception.PaymentVerificationException;
import com.pcgear.complink.pcgear.Payment.model.AccessTokenResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;
import com.pcgear.complink.pcgear.Payment.model.SingleInquiryResponse;
import com.pcgear.complink.pcgear.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.Sell.SellService;
import com.pcgear.complink.pcgear.User.dto.SubscriptionStatus;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import io.portone.sdk.server.webhook.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookVerifier;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final PortoneProperties portoneProperties;

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    private final SellService sellService;
    private final OrderService orderService;
    private final ItemService itemService;

    private final WebClient webClient;

    public Mono<OrderPayment> executeImmediatePayment(UserEntity user, SubscriptionRequest subscriptionRequest) {
        log.info("단건결제==========================================================");

        final String paymentId = "payment-" + UUID.randomUUID().toString();
        // API 경로: /payments/{payment_id}/billing-key
        final String uri = String.format(portoneProperties.getApiUrl() + "/payments/" + paymentId + "/billing-key");

        // 요청 본문 구성
        Map<String, Object> requestBody = Map.of(
                "billingKey", subscriptionRequest.getBillingKey(),
                "orderName", "정기결제(첫번째)",
                "customer", createCustomerMap(user),
                "amount", Map.of("total", subscriptionRequest.getAmount()),
                "currency", "KRW");

        // 1. WebClient 호출 (Mono<String> 반환)
        return webClient.post()
                .uri(uri)
                .header("Authorization",
                        "PortOne " + portoneProperties.getApiSecret())
                .contentType(MediaType.APPLICATION_JSON) // JSON으로 요청 본문을 보냅니다.
                .bodyValue(requestBody)
                .retrieve()
                // HTTP 상태 코드 4xx, 5xx 발생 시 예외 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.error("PortOne API 오류 발생! 상태: {}, 본문: {}", response.statusCode(), body);
                            String errorMessage = "결제 실패: " + body;
                            return Mono.error(new RuntimeException(errorMessage));
                        }))
                // 2. 응답 본문(JSON 문자열)을 String으로 받아옵니다.
                .bodyToMono(String.class)
                // 3. 응답 문자열을 파싱하고 DB 저장 (map 연산자로 동기적 처리)
                .map(responseBody -> {
                    try {
                        log.info("responseBody: {}", responseBody);

                        // 응답을 Map<String, Object> 타입으로 변환
                        Map<String, Object> responseBodyMap = objectMapper.readValue(responseBody,
                                new TypeReference<>() {
                                });

                        // ⭐️ 1. "payment" 키로 중첩된 Map을 가져옵니다.
                        // (안전한 접근을 위해 null 체크 로직을 포함해야 하지만, 일단 강제 캐스팅으로 수정합니다.)
                        Object paymentObject = responseBodyMap.get("payment");
                        Map<String, Object> paymentDetail = (Map<String, Object>) paymentObject;

                        // ⭐️ 2. paymentDetail Map에서 "paidAt"을 추출합니다.
                        String paidAtString = (String) paymentDetail.get("paidAt");
                        log.info("추출된 paidAt: {}", paidAtString);

                        Instant instant = Instant.parse(paidAtString);
                        LocalDateTime paidAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                        OrderPayment payment = OrderPayment.builder()
                                .paymentId(paymentId)
                                .userId(user.getUsername())
                                .amount(subscriptionRequest.getAmount())
                                .paymentMethod(subscriptionRequest.getBillingKeyMethod())
                                .paymentStatus(PaymentStatus.READY)
                                .paidAt(paidAt)
                                .build();
                        return paymentRepository.save(payment);

                    } catch (Exception e) {
                        log.error("결제 응답 파싱 실패", e);
                        throw new RuntimeException("결제 응답 처리 중 오류 발생", e);
                    }
                });
    }

    // Customer 정보 Map 생성을 위한 헬퍼 메서드
    private Map<String, Object> createCustomerMap(UserEntity user) {
        return Map.of(
                "id", user.getUsername().toString(),
                "name", Map.of("full", user.getName()),
                "email", user.getEmail());
    }

    @Transactional
    public void webhookVerify(String payload, String webhookId, String webhookSignature, String webhookTimestamp)
            throws WebhookVerificationException {
        log.info("웹훅검증==========================================================");
        // 1. 웹훅을 보낸 이가 포트원이 맞는지 검증합니다.
        WebhookVerifier verifier = new WebhookVerifier(portoneProperties.getWebhookSecret());
        try {
            verifier.verify(payload, webhookId, webhookSignature, webhookTimestamp);
            log.info("WebhookVerifier 검증 성공: {}", webhookId);
        } catch (WebhookVerificationException e) {
            e.printStackTrace();
            throw new WebhookVerificationException(webhookTimestamp, e);
        }

        // 2. 웹훅 타입이 Transaction.Paid 일 때만 금액 비교 검증을 합니다.
        Map<String, Object> webhookData = null;
        try {
            webhookData = objectMapper.readValue(payload, new TypeReference<>() {
            });

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String webhookType = (String) webhookData.get("type");
        if (!webhookType.equals("Transaction.Paid")) {
            return;
        }

        // 3-1. 포트원에서 단건내역을 조회합니다.
        String paymentId = extractPaymentIdFromPayload(payload);
        Map<String, Object> paymentDetail = webClient
                .get()
                .uri(portoneProperties.getApiUrl() + "/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + portoneProperties.getApiSecret())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        log.info("paymentDetail: {}", paymentDetail);

        // 3-2. db에서 단건내역을 조회합니다.
        Optional<OrderPayment> existingPayment = paymentRepository.findByPaymentId(paymentId);

        OrderPayment dbPayment;
        boolean isNewPayment = existingPayment.isEmpty();

        // 3-3. db에 내역이 있다면 단건 결제에 대한 웹훅 입니다.
        if (!isNewPayment) {
            log.info("단건 결제에 대한 웹훅 입니다.");
            dbPayment = existingPayment.get();

            // 4. 결제 금액을 비교합니다.
            Integer apiAmount = (Integer) ((Map<String, Object>) paymentDetail.get("amount")).get("total");
            Integer dbOrderAmount = dbPayment.getAmount();

            if (!apiAmount.equals(dbOrderAmount)) {
                log.error("금액 위변조 의심: 주문 금액({})과 실제 결제 금액({})이 일치하지 않습니다.", dbOrderAmount,
                        apiAmount);
                throw new PaymentVerificationException("결제 금액 불일치");
            } else {
                log.info("단건 결제 금액 일치합니다. 다음 정기 결제를 예약합니다.");
            }

            String apiStatus = (String) paymentDetail.get("status");
            PaymentStatus apiStatusEnum = PaymentStatus.fromNameIgnoreCase(apiStatus);

            // DBStatus== READY|| FAILED|| CANCELLED && APIStatus==PAID일 때만 로직 실행
            if (PaymentStatus.PAID.equals(apiStatusEnum) &&
                    !PaymentStatus.PAID.equals(dbPayment.getPaymentStatus())) {
                dbPayment.setPaymentStatus(PaymentStatus.PAID);
                paymentRepository.save(dbPayment);
                log.info("✅ 결제 ID {}의 상태를 PAID로 성공적으로 업데이트했습니다.", paymentId);

            } else {
                log.warn("이미 처리된 결제이거나(DB 상태: {}), API 상태가 PAID가 아닙니다(API 상태: {}).",
                        dbPayment.getPaymentStatus().name(), apiStatus);
            }

            // log.info("정기 결제 예약 스케줄링이 완료되었습니다.");

            // 3-3. db에 내역이 없다면 예약된 거의 결제에 대한 웹훅 입니다.
        } else {
            log.info("예약 결제건에 대한 웹훅입니다.");

            Integer apiAmount = (Integer) ((Map<String, Object>) paymentDetail.get("amount")).get("total");
            String apiOrderName = (String) paymentDetail.get("orderName");

            String trackingId = extractTrackingIdFromOrderName(apiOrderName);
            if (trackingId == null) {
                log.error("OrderName에서 trackingId ID를 추출할 수 없습니다: {}", apiOrderName);
                throw new PaymentVerificationException("trackingId누락");
            }

            Subscription subscription = subscriptionRepository.findByTrackingId(trackingId)
                    .orElseThrow(() -> new EntityNotFoundException("매칭되는 Subscription을 찾을 수 없습니다: " + trackingId));
            Integer dbSubscriptionAmount = subscription.getAmount();

            if (!apiAmount.equals(dbSubscriptionAmount)) {
                log.error("정기 결제 금액 위변조 의심: 구독 금액({})과 실제 결제 금액({})이 불일치합니다.",
                        dbSubscriptionAmount, apiAmount);
                throw new PaymentVerificationException("결제 금액 불일치");
            } else {
                log.info("정기 결제 금액 일치합니다. 다음 정기 결제를 예약합니다.");
            }
            dbPayment = OrderPayment.builder()
                    .paymentId((String) paymentDetail.get("id")) // paymentId가 "id" 필드에 있음
                    .amount((Integer) ((Map<String, Object>) paymentDetail.get("amount")).get("total"))
                    .paymentMethod((String) ((Map<String, Object>) paymentDetail.get("method")).get("provider"))
                    .paymentStatus(PaymentStatus.PAID)
                    .paidAt(LocalDateTime.ofInstant(
                            Instant.parse((String) paymentDetail.get("paidAt")),
                            ZoneId.systemDefault()))
                    .build();
        }

        // 사용자의 구독 상태를 ACTIVE로 변경합니다.
        UserEntity userEntity = userRepository.findByUsername(dbPayment.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (userEntity.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            userEntity.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            userRepository.save(userEntity);
            log.info("✅ 사용자 {}의 구독 상태를 ACTIVE로 변경했습니다.", userEntity.getUsername());
        }

        // 5. 다음 정기결제를 예약합니다.
        SubscriptionRequest subscriptionRequest = SubscriptionRequest.builder()
                .billingKey((String) paymentDetail.get("billingKey"))
                .amount(((Integer) ((Map<String, Object>) paymentDetail.get("amount")).get("total")))
                .build();

        subscriptionService.scheduleNextPayment(userEntity,
                subscriptionRequest, 5).block();

    }

    private String extractPaymentIdFromPayload(String payload) {
        try {
            Map<String, Object> payloadMap = objectMapper.readValue(payload, new TypeReference<>() {
            });

            Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");

            String paymentId = (String) data.get("paymentId");
            log.info("✅ 최종 추출된 paymentId: {}", paymentId);

            if (paymentId == null) {
                throw new RuntimeException("페이로드에서 paymentId를 찾을 수 없습니다.");
            }
            return paymentId;

        } catch (JsonProcessingException e) {
            log.error("웹훅 JSON 파싱 중 오류가 발생했습니다.", e);
            // 파싱에 실패하면 로직을 중단시켜야 하므로 예외를 던집니다.
            throw new RuntimeException("웹훅 JSON 파싱 실패", e);
        }
    }

    private String extractTrackingIdFromOrderName(String orderName) {
        if (orderName == null) {
            return null;
        }
        try {
            // 1. 마지막 '#'의 위치를 찾습니다.
            int hashIndex = orderName.lastIndexOf('#');

            // 2. '#'이 없으면 유효한 ID가 없으므로 null 반환
            if (hashIndex < 0) {
                return null;
            }
            // 3. '#' 다음 문자열(UUID)을 추출하고 공백을 제거합니다.
            String trackingIdString = orderName.substring(hashIndex + 1).trim();

            // 4. 추출된 문자열을 반환합니다. (UUID는 String 타입이므로 파싱 불필요)
            // (선택 사항: 추출된 문자열이 UUID 형식인지 추가 검증 로직을 넣을 수도 있습니다.)
            return trackingIdString;

        } catch (StringIndexOutOfBoundsException e) {
            // '#'은 있지만 그 뒤에 아무 문자도 없을 경우 (발생 가능성은 낮음)
            return null;
        }
    }

    public SingleInquiryResponse getSingleInquiry(String impUid, String accessToken) {
        return webClient.get()
                .uri("https://api.iamport.kr/payments/" + impUid)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SingleInquiryResponse.class)
                .block();
    }

    public String getAccessToken() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("imp_key", portoneProperties.getImpKey());
        requestBody.put("imp_secret", portoneProperties.getImpSecret());

        return webClient.post()
                .uri(portoneProperties.getAccessTokenUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .map(response -> {
                    if (response != null && response.getResponse() != null) {
                        return response.getResponse().getAccessToken();
                    }
                    throw new RuntimeException("Failed to get access token: Response or response data is null.");
                })
                .block();
    }

    public void cancelPayment() {

    }

    @Transactional
    public void finalizeOrderPayment(Order order) {

        // 1. 판매 기록 생성 (매출 테이블에 반영)
        sellService.createSell(order);

        // 2. 주문 상태를 상품준비중으로 업데이트
        orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PAID);

        // 3. 주문 결제 날짜를 설정
        orderService.setPaidAt(order);

        // 4. 재고 차감 (재고 수량(QOH)을 업데이트)
        // 이 과정에서 재고 부족 등으로 예외가 발생하면 전체 트랜잭션이 롤백됩니다.
        itemService.updateItemAvailableQuantity(order);

        // 5. 결제기록 생성
        createPayment(order);

    }

    private void createPayment(Order order) {
        final String paymentId = "payment-" + UUID.randomUUID().toString();
        OrderPayment payment = OrderPayment.builder()
                .paymentId(paymentId)
                .order(order)
                .userId("AAA")
                .amount(order.getGrandAmount().intValue())
                .paymentMethod("EASY_PAY")
                .paymentStatus(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
    }
}