package com.pcgear.complink.pcgear.Payment; // 실제 프로젝트의 패키지 경로로 수정하세요.

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.Payment.exception.PaymentVerificationException;
import com.pcgear.complink.pcgear.Payment.model.AccessTokenResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;
import com.pcgear.complink.pcgear.Payment.model.SingleInquiryResponse;
import com.pcgear.complink.pcgear.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.Payment.model.WebhookRequest;
import com.pcgear.complink.pcgear.Sell.SellService;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;
import com.pcgear.complink.pcgear.User.service.MailService;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import io.portone.sdk.server.webhook.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookVerifier;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
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
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    private PaymentService self;
    private final SellService sellService;
    private final OrderService orderService;
    private final ItemService itemService;
    private final PaymentLinkService paymentLinkService;
    private final RestClient restClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;

    // public Mono<OrderPayment> executeImmediatePayment(UserEntity user,
    // SubscriptionRequest subscriptionRequest) {
    // log.info("단건결제==========================================================");

    // final String paymentId = "payment-" + UUID.randomUUID().toString();
    // // API 경로: /payments/{payment_id}/billing-key
    // final String uri = String.format(portoneProperties.getApiUrl() + "/payments/"
    // + paymentId + "/billing-key");

    // // 요청 본문 구성
    // Map<String, Object> requestBody = Map.of(
    // "billingKey", subscriptionRequest.getBillingKey(),
    // "orderName", "정기결제(첫번째)",
    // "customer", createCustomerMap(user),
    // "amount", Map.of("total", subscriptionRequest.getAmount()),
    // "currency", "KRW");

    // // 1. WebClient 호출 (Mono<String> 반환)
    // return webClient.post()
    // .uri(uri)
    // .header("Authorization",
    // "PortOne " + portoneProperties.getApiSecret())
    // .contentType(MediaType.APPLICATION_JSON) // JSON으로 요청 본문을 보냅니다.
    // .bodyValue(requestBody)
    // .retrieve()
    // // HTTP 상태 코드 4xx, 5xx 발생 시 예외 처리
    // .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
    // response -> response.bodyToMono(String.class).flatMap(body -> {
    // log.error("PortOne API 오류 발생! 상태: {}, 본문: {}", response.statusCode(), body);
    // String errorMessage = "결제 실패: " + body;
    // return Mono.error(new RuntimeException(errorMessage));
    // }))
    // // 2. 응답 본문(JSON 문자열)을 String으로 받아옵니다.
    // .bodyToMono(String.class)
    // // 3. 응답 문자열을 파싱하고 DB 저장 (map 연산자로 동기적 처리)
    // .map(responseBody -> {
    // try {
    // log.info("responseBody: {}", responseBody);

    // // 응답을 Map<String, Object> 타입으로 변환
    // Map<String, Object> responseBodyMap = objectMapper.readValue(responseBody,
    // new TypeReference<>() {
    // });

    // // ⭐️ 1. "payment" 키로 중첩된 Map을 가져옵니다.
    // // (안전한 접근을 위해 null 체크 로직을 포함해야 하지만, 일단 강제 캐스팅으로 수정합니다.)
    // Object paymentObject = responseBodyMap.get("payment");
    // Map<String, Object> paymentDetail = (Map<String, Object>) paymentObject;

    // // ⭐️ 2. paymentDetail Map에서 "paidAt"을 추출합니다.
    // String paidAtString = (String) paymentDetail.get("paidAt");
    // log.info("추출된 paidAt: {}", paidAtString);

    // Instant instant = Instant.parse(paidAtString);
    // LocalDateTime paidAt = LocalDateTime.ofInstant(instant,
    // ZoneId.systemDefault());

    // OrderPayment payment = OrderPayment.builder()
    // .paymentId(paymentId)
    // .userId(user.getUsername())
    // .amount(subscriptionRequest.getAmount())
    // .paymentMethod(subscriptionRequest.getBillingKeyMethod())
    // .paymentStatus(PaymentStatus.READY)
    // .paidAt(paidAt)
    // .build();
    // return paymentRepository.save(payment);

    // } catch (Exception e) {
    // log.error("결제 응답 파싱 실패", e);
    // throw new RuntimeException("결제 응답 처리 중 오류 발생", e);
    // }
    // });
    // }

    public OrderPayment executeImmediatePayment(UserEntity user, SubscriptionRequest subscriptionRequest) {
        log.info("단건(즉시) 결제 요청 시작 ==========================================");

        final String paymentId = "payment-" + UUID.randomUUID().toString();
        final String uri = String.format(portoneProperties.getApiUrl() + "/payments/" + paymentId + "/billing-key");

        Map<String, Object> requestBody = Map.of(
                "billingKey", subscriptionRequest.getBillingKey(),
                "orderName", "즉시결제",
                "customer", createCustomerMap(user),
                "amount", Map.of("total", subscriptionRequest.getAmount()),
                "currency", "KRW");

        try {
            // [RestClient] 동기 호출
            // 응답을 Map으로 바로 받음 (JSON 파싱 불필요)
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

    private OrderPayment savePaymentFromResponse(Map<String, Object> responseMap, String paymentId, UserEntity user,
            SubscriptionRequest request) {
        Map<String, Object> paymentDetail = (Map<String, Object>) responseMap.get("payment");
        OrderPayment payment = OrderPayment.builder()
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

    // Customer 정보 Map 생성을 위한 헬퍼 메서드
    private Map<String, Object> createCustomerMap(UserEntity user) {
        return Map.of(
                "id", user.getUsername().toString(),
                "name", Map.of("full", user.getName()),
                "email", user.getEmail());
    }

    // @Transactional
    // public void webhookVerify(String payload, String webhookId, String
    // webhookSignature, String webhookTimestamp)
    // throws WebhookVerificationException {
    // log.info("웹훅검증==========================================================");
    // // 1. 웹훅을 보낸 이가 포트원이 맞는지 검증합니다.
    // WebhookVerifier verifier = new
    // WebhookVerifier(portoneProperties.getWebhookSecret());
    // try {
    // verifier.verify(payload, webhookId, webhookSignature, webhookTimestamp);
    // log.info("WebhookVerifier 검증 성공: {}", webhookId);
    // } catch (WebhookVerificationException e) {
    // e.printStackTrace();
    // throw new WebhookVerificationException(webhookTimestamp, e);
    // }

    // // 2. 웹훅 타입이 Transaction.Paid 일 때만 금액 비교 검증을 합니다.
    // Map<String, Object> webhookData = null;
    // try {
    // webhookData = objectMapper.readValue(payload, new TypeReference<>() {
    // });

    // } catch (JsonMappingException e) {
    // e.printStackTrace();
    // } catch (JsonProcessingException e) {
    // e.printStackTrace();
    // }
    // String webhookType = (String) webhookData.get("type");
    // if (!webhookType.equals("Transaction.Paid")) {
    // return;
    // }

    // // 3-1. 포트원에서 단건내역을 조회합니다.
    // String paymentId = extractPaymentIdFromPayload(payload);
    // Map<String, Object> paymentDetail = webClient
    // .get()
    // .uri(portoneProperties.getApiUrl() + "/payments/{paymentId}", paymentId)
    // .header("Authorization", "PortOne " + portoneProperties.getApiSecret())
    // .retrieve()
    // .bodyToMono(Map.class)
    // .block();
    // log.info("paymentDetail: {}", paymentDetail);

    // // 3-2. db에서 단건내역을 조회합니다.
    // Optional<OrderPayment> existingPayment =
    // paymentRepository.findByPaymentId(paymentId);

    // OrderPayment dbPayment;
    // boolean isNewPayment = existingPayment.isEmpty();

    // // 3-3. db에 내역이 있다면 단건 결제에 대한 웹훅 입니다.
    // if (!isNewPayment) {
    // log.info("단건 결제에 대한 웹훅 입니다.");
    // dbPayment = existingPayment.get();

    // // 4. 결제 금액을 비교합니다.
    // Integer apiAmount = (Integer) ((Map<String, Object>)
    // paymentDetail.get("amount")).get("total");
    // Integer dbOrderAmount = dbPayment.getAmount();

    // if (!apiAmount.equals(dbOrderAmount)) {
    // log.error("금액 위변조 의심: 주문 금액({})과 실제 결제 금액({})이 일치하지 않습니다.", dbOrderAmount,
    // apiAmount);
    // throw new PaymentVerificationException("결제 금액 불일치");
    // } else {
    // log.info("단건 결제 금액 일치합니다. 다음 정기 결제를 예약합니다.");
    // }

    // String apiStatus = (String) paymentDetail.get("status");
    // PaymentStatus apiStatusEnum = PaymentStatus.fromNameIgnoreCase(apiStatus);

    // // DBStatus== READY|| FAILED|| CANCELLED && APIStatus==PAID일 때만 로직 실행
    // if (PaymentStatus.PAID.equals(apiStatusEnum) &&
    // !PaymentStatus.PAID.equals(dbPayment.getPaymentStatus())) {
    // dbPayment.setPaymentStatus(PaymentStatus.PAID);
    // paymentRepository.save(dbPayment);
    // log.info("✅ 결제 ID {}의 상태를 PAID로 성공적으로 업데이트했습니다.", paymentId);

    // } else {
    // log.warn("이미 처리된 결제이거나(DB 상태: {}), API 상태가 PAID가 아닙니다(API 상태: {}).",
    // dbPayment.getPaymentStatus().name(), apiStatus);
    // }

    // // log.info("정기 결제 예약 스케줄링이 완료되었습니다.");

    // // 3-3. db에 내역이 없다면 예약된 거의 결제에 대한 웹훅 입니다.
    // } else {
    // log.info("예약 결제건에 대한 웹훅입니다.");

    // Integer apiAmount = (Integer) ((Map<String, Object>)
    // paymentDetail.get("amount")).get("total");
    // String apiOrderName = (String) paymentDetail.get("orderName");

    // String trackingId = extractTrackingIdFromOrderName(apiOrderName);
    // if (trackingId == null) {
    // log.error("OrderName에서 trackingId ID를 추출할 수 없습니다: {}", apiOrderName);
    // throw new PaymentVerificationException("trackingId누락");
    // }

    // Subscription subscription =
    // subscriptionRepository.findByTrackingId(trackingId)
    // .orElseThrow(() -> new EntityNotFoundException("매칭되는 Subscription을 찾을 수 없습니다:
    // " + trackingId));
    // Integer dbSubscriptionAmount = subscription.getAmount();

    // if (!apiAmount.equals(dbSubscriptionAmount)) {
    // log.error("정기 결제 금액 위변조 의심: 구독 금액({})과 실제 결제 금액({})이 불일치합니다.",
    // dbSubscriptionAmount, apiAmount);
    // throw new PaymentVerificationException("결제 금액 불일치");
    // } else {
    // log.info("정기 결제 금액 일치합니다. 다음 정기 결제를 예약합니다.");
    // }
    // dbPayment = OrderPayment.builder()
    // .paymentId((String) paymentDetail.get("id")) // paymentId가 "id" 필드에 있음
    // .amount((Integer) ((Map<String, Object>)
    // paymentDetail.get("amount")).get("total"))
    // .paymentMethod((String) ((Map<String, Object>)
    // paymentDetail.get("method")).get("provider"))
    // .paymentStatus(PaymentStatus.PAID)
    // .paidAt(LocalDateTime.ofInstant(
    // Instant.parse((String) paymentDetail.get("paidAt")),
    // ZoneId.systemDefault()))
    // .build();
    // }

    // // 사용자의 구독 상태를 ACTIVE로 변경합니다.
    // UserEntity userEntity = userRepository.findByUsername(dbPayment.getUserId())
    // .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    // if (userEntity.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
    // userEntity.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
    // userRepository.save(userEntity);
    // log.info("✅ 사용자 {}의 구독 상태를 ACTIVE로 변경했습니다.", userEntity.getUsername());
    // }

    // // 5. 다음 정기결제를 예약합니다.
    // SubscriptionRequest subscriptionRequest = SubscriptionRequest.builder()
    // .billingKey((String) paymentDetail.get("billingKey"))
    // .amount(((Integer) ((Map<String, Object>)
    // paymentDetail.get("amount")).get("total")))
    // .build();

    // subscriptionService.scheduleNextPayment(userEntity,
    // subscriptionRequest, 5).block();

    // }

    public void webhookVerify(String payload, String webhookId, String webhookSignature, String webhookTimestamp)
            throws WebhookVerificationException {
        log.info("웹훅 처리 시작 ==========================================");

        // 1. 서명 검증
        verifyWebhookSignature(payload, webhookId, webhookSignature, webhookTimestamp);

        // 2. 페이로드 파싱
        Map<String, Object> webhookData = parsePayload(payload);
        if (!"Transaction.Paid".equals(webhookData.get("type"))) {
            log.info("결제 완료(Paid) 이벤트가 아니므로 무시합니다.");
            return;
        }

        String paymentId = extractPaymentIdFromPayload(payload);

        try {
            // 3. [외부 API] 결제 상세 내역 조회 (동기 - RestClient 사용)
            // AccessToken이 필요하면 getAccessToken() 호출 후 헤더에 추가
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
        // V2 API (Secret Key 헤더 사용) 예시
        return restClient.get()
                .uri(portoneProperties.getApiUrl() + "/payments/" + paymentId)
                .header("Authorization", "PortOne " + portoneProperties.getApiSecret())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
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

    @Transactional
    public void processWebhookTransaction(String paymentId, Map<String, Object> paymentDetail) {
        Optional<OrderPayment> existingPaymentOpt = paymentRepository.findByPaymentId(paymentId);

        if (existingPaymentOpt.isPresent()) {
            log.info(">>> [TYPE: 단건/주문 결제] 처리");
            processOneTimePayment(existingPaymentOpt.get(), paymentDetail);
        } else {
            log.info(">>> [TYPE: 정기/구독 결제] 처리");
            processSubscriptionPayment(paymentDetail);
        }
    }

    private void processOneTimePayment(OrderPayment dbPayment, Map<String, Object> paymentDetail) {
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
                finalizeOrderPayment(dbPayment.getOrder());
            }
        }
    }

    private void processSubscriptionPayment(Map<String, Object> paymentDetail) {
        String orderName = (String) paymentDetail.get("orderName");
        String trackingId = extractTrackingIdFromOrderName(orderName);

        Subscription subscription = subscriptionRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new EntityNotFoundException("구독 정보 없음"));

        // ... (구독 결제 처리 로직 - 저장 등) ...
        // RestClient 사용 시 block() 불필요, 그냥 호출하면 됨
        // subscriptionService.scheduleNextPayment(...);
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
        try {
            return restClient.get()
                    .uri("https://api.iamport.kr/payments/" + impUid)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(SingleInquiryResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            // 404 에러 발생 시 로그 찍고 null 반환하거나 커스텀 예외 던지기
            log.error("포트원에서 결제 정보를 찾을 수 없습니다. impUid={}", impUid);
            throw new RuntimeException("결제 정보 없음 (포트원)", e);
        }
    }

    public String getAccessToken() {
        Map<String, String> body = new HashMap<>();
        body.put("imp_key", portoneProperties.getImpKey());
        body.put("imp_secret", portoneProperties.getImpSecret());

        return restClient.post()
                .uri(portoneProperties.getAccessTokenUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AccessTokenResponse.class) // 바로 객체 반환
                .getResponse().getAccessToken();
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

    public void processPaymentLinkWebhook(WebhookRequest webhookRequest) {
        log.info("결제 링크 웹훅 처리 시작: {}", webhookRequest);

        // 이미 처리된 결제건인지 확인
        Order order = orderRepository.findByMerchantUid(webhookRequest.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException(
                        "주문을 찾을 수 없습니다. MerchantUid: " + webhookRequest.getMerchantUid()));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            log.info("이미 처리된 결제건입니다. (중복 웹훅 무시) OrderId: {}", order.getOrderId());
            return;
        }

        String accessToken = null;

        try {
            // 1. 액세스 토큰 발급
            accessToken = getAccessToken();

            // 2. 결제 단건 조회
            SingleInquiryResponse.ResponseData paymentData = getSingleInquiry(
                    webhookRequest.getImpUid(), accessToken).getResponse();

            log.info("포트원 조회 결과: status={}, amount={}", paymentData.getStatus(), paymentData.getAmount());

            // 3. 검증 및 DB 저장 (트랜잭션 시작)
            self.processLinkWebhookTransaction(webhookRequest, paymentData);

        } catch (PaymentVerificationException e) {
            log.error("⛔ 금액 불일치! 결제 취소 실행: {}", e.getMessage());

            if (accessToken != null) {
                paymentLinkService.cancelPayment(accessToken, webhookRequest.getImpUid(), "금액 불일치");
            } else {
                log.error("액세스 토큰이 없어 결제 취소를 수행할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("🔥 시스템 오류", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void processLinkWebhookTransaction(WebhookRequest webhookRequest,
            SingleInquiryResponse.ResponseData paymentData) {

        // 1. 주문 조회
        Order order = orderRepository.findByMerchantUid(webhookRequest.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException(
                        "주문을 찾을 수 없습니다. MerchantUid: " + webhookRequest.getMerchantUid()));

        // 2. 금액 검증
        BigDecimal amountToBePaid = order.getGrandAmount();
        BigDecimal paidAmount = paymentData.getAmount();

        if (paidAmount.compareTo(amountToBePaid) != 0) {
            log.error("위변조 감지! 주문금액: {}, 결제금액: {}", amountToBePaid, paidAmount);
            throw new PaymentVerificationException("결제 금액 불일치 (위조된 결제 시도)");
        }

        // 3. 포트원 UID 저장
        order.setImpUid(webhookRequest.getImpUid());
        String paymentStatus = paymentData.getStatus();

        // 4. 결제 상태에 따른 분기 처리
        switch (paymentStatus) {
            case "paid": // 결제 완료
                finalizeOrderPayment(order); // 재고 차감, 매출 생성 등
                sendNotification(order, "결제가 완료되었습니다.");
                log.info("Payment completed for order {}", webhookRequest.getMerchantUid());
                break;

            case "cancelled": // 결제 취소
                orderService.updateOrderStatus(order.getOrderId(), OrderStatus.CANCELLED);
                log.info("Payment cancelled for order {}", webhookRequest.getMerchantUid());
                break;

            case "failed": // 결제 실패
                orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PAYMENT_FAILED);
                log.info("Payment failed for order {}", webhookRequest.getMerchantUid());
                break;

            default:
                log.warn("Unknown payment status: {}", paymentStatus);
        }
    }

    @Transactional
    public void finalizeOrderPayment(Order order) {
        // 1. 판매 기록 생성 (매출 테이블에 반영)
        sellService.createSell(order);

        // 2. 주문 상태를 상품준비중으로 업데이트
        orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PAID);

        // 3. 주문 결제 날짜를 설정
        orderService.setPaidAt(order);

        // 4. 재고 차감
        itemService.updateItemAvailableQuantity(order);

        // 5. 결제기록 생성
        createPayment(order);

    }

    // 알림 전송 헬퍼 메서드 (트랜잭션에 영향 안 주게 예외 처리)
    private void sendNotification(Order order, String msgBody) {
        try {
            String message = "주문번호: " + order.getOrderId() + "번의 " + msgBody + " 판매조회에서 확인해주세요.";
            messagingTemplate.convertAndSend("/topic/notifications", message);
        } catch (Exception e) {
            log.error("알림 전송 실패 (결제 로직은 성공함)", e);
        }
    }

}