package com.pcgear.complink.pcgear.Payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.Payment.exception.PaymentVerificationException;
import com.pcgear.complink.pcgear.Payment.model.AccessTokenResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkRequest;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1AccessTokenReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelResp;
import com.pcgear.complink.pcgear.Payment.model.SingleInquiryResponse;
import com.pcgear.complink.pcgear.Payment.model.WebhookRequest;
import com.pcgear.complink.pcgear.Sell.SellService;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.pcgear.complink.pcgear.config.SseEmitterManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLinkService {

        @Lazy
        @Autowired
        private PaymentLinkService self;
        private final RestClient restClient;
        private final PortoneProperties portoneProperties;
        private final OrderRepository orderRepository;
        private final SseEmitterManager sseEmitterManager;

        private final SellService sellService;
        private final OrderService orderService;
        private final ItemService itemService;

        private final PaymentRepository paymentRepository;
        private final ObjectMapper objectMapper;

        private static final String CREATE_PAYMENT_LINK_URI = "https://api.iamport.co/api/supplements/v1/link/payment";
        private static final String PORTONE_V1_CANCEL_PAYMENT_URI = "https://api.iamport.kr/payments/cancel";

        // 결제링크 생성
        public String createPaymentLink(String merchantUid, int amount, String productName, String buyerTel) {
                log.info("결제 링크 생성 요청: merchantUid={}, amount={}", merchantUid, amount);

                // 1. 요청 객체 생성 (DTO 활용)
                PaymentLinkRequest.PaymentInfo.PayMethod cardPayMethod = PaymentLinkRequest.PaymentInfo.PayMethod
                                .builder()
                                .pg("tosspay_v2")
                                .pay_method("tosspay_money")
                                .label("토스페이")
                                .build();

                PaymentLinkRequest.PaymentInfo paymentInfo = PaymentLinkRequest.PaymentInfo.builder()
                                .title("PCGear 주문 결제")
                                .user_code(portoneProperties.getImpUserCode())
                                .amount(amount)
                                .merchant_uid(merchantUid)
                                .name(productName)
                                .currency("KRW")
                                .buyer_tel(buyerTel)
                                .notice_url(portoneProperties.getWebhookUrl()
                                                + "/api/payment/webhook/verify/paymentLink")
                                .pay_methods(Collections.singletonList(cardPayMethod))
                                .build();

                // 2. 최종 요청 본문 생성
                String paymentInfoJsonString;
                try {
                        paymentInfoJsonString = objectMapper.writeValueAsString(paymentInfo);
                } catch (Exception e) {
                        throw new RuntimeException("PaymentInfo 직렬화 실패", e);
                }

                long expiredAt = Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond();

                PaymentLinkRequest requestBody = new PaymentLinkRequest();
                requestBody.setPaymentInfo(paymentInfoJsonString); // 문자열로 설정
                requestBody.setExpired_at(expiredAt);

                try {
                        // 3. API 호출 (RestClient)
                        PaymentLinkResponse response = restClient.post()
                                        .uri(CREATE_PAYMENT_LINK_URI)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(requestBody) // 객체를 넣으면 알아서 JSON으로 변환됨
                                        .retrieve()
                                        .body(PaymentLinkResponse.class); // 응답도 객체로 바로 받음

                        if (response != null && response.getShortenedUrl() != null) {
                                log.info("생성된 단축 URL: {}", response.getShortenedUrl());
                                return response.getShortenedUrl();
                        } else {
                                throw new RuntimeException("결제 링크 생성 실패: 응답 없음");
                        }

                } catch (Exception e) {
                        log.error("결제 링크 생성 API 호출 중 오류", e);
                        throw new RuntimeException("결제 링크 생성 실패", e);
                }
        }

        public PortoneV1CancelResp cancelPayment(Integer orderId, String reason) {
                String accessToken = getAccessToken();

                String impUid = orderRepository.findById(orderId)
                                .map(Order::getImpUid)
                                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

                return cancelPortonePayment(accessToken, impUid, reason);
        }

        private PortoneV1CancelResp cancelPortonePayment(String accessToken, String impUid, String reason) {
                PortoneV1CancelReq request = new PortoneV1CancelReq(impUid, reason);

                try {
                        PortoneV1CancelResp response = restClient.post()
                                        .uri(PORTONE_V1_CANCEL_PAYMENT_URI)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(request)
                                        .retrieve()
                                        .body(PortoneV1CancelResp.class);

                        if (response != null && response.getCode() != 0) {
                                log.error("포트원 결제 취소 실패: {}", response.getMessage());
                                throw new RuntimeException("결제 취소 실패: " + response.getMessage());
                        }

                        return response;

                } catch (Exception e) {
                        log.error("결제 취소 API 호출 중 오류", e);
                        throw new RuntimeException("결제 취소 실패", e);
                }
        }

        private String getAccessToken() {
                PortoneV1AccessTokenReq request = new PortoneV1AccessTokenReq(
                                portoneProperties.getImpKey(),
                                portoneProperties.getImpSecret());

                try {
                        AccessTokenResponse response = restClient.post()
                                        .uri(portoneProperties.getAccessTokenUrl())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(request)
                                        .retrieve()
                                        .body(AccessTokenResponse.class); // DTO 바로 매핑 (이름 주의: AccessTokenResp vs
                                                                          // PortoneV1AccessTokenResp)

                        if (response != null && response.getResponse() != null) {
                                return response.getResponse().getAccessToken();
                        }
                        throw new RuntimeException("토큰 발급 응답이 올바르지 않습니다.");

                } catch (Exception e) {
                        log.error("토큰 발급 실패", e);
                        throw new RuntimeException("토큰 발급 실패", e);
                }
        }

        public String cancelPaymentLink(String paymentLink) {
                String linkId = paymentLink.substring(paymentLink.lastIndexOf("/") + 1);
                String uri = "https://api.iamport.co/api/supplements/v1/link/payment/" + linkId;

                try {
                        // PUT 요청
                        restClient.put()
                                        .uri(uri)
                                        .retrieve()
                                        .body(String.class); // 응답 내용이 중요하지 않음 ("{}" 등)

                        return "결제 링크가 성공적으로 만료(취소)되었습니다.";

                } catch (Exception e) {
                        log.error("링크 만료 실패: {}", e.getMessage());
                        throw new RuntimeException("링크 만료 실패", e);
                }
        }

        public void verifyWebhook(WebhookRequest webhookRequest) {
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
                        self.verifyPaidAmountAndProcessPayment(webhookRequest, paymentData);

                } catch (PaymentVerificationException e) {
                        log.error("⛔ 금액 불일치! 결제 취소 실행: {}", e.getMessage());

                        if (accessToken != null) {
                                cancelPortonePayment(accessToken, webhookRequest.getImpUid(), "금액 불일치");
                        } else {
                                log.error("액세스 토큰이 없어 결제 취소를 수행할 수 없습니다.");
                        }
                } catch (Exception e) {
                        log.error("🔥 시스템 오류", e);
                        throw new RuntimeException(e);
                }
        }

        private SingleInquiryResponse getSingleInquiry(String impUid, String accessToken) {
                try {
                        return restClient.get()
                                        .uri("https://api.iamport.kr/payments/" + impUid + "?include_sandbox=true")
                                        .header("Authorization", "Bearer " + accessToken)
                                        .retrieve()
                                        .body(SingleInquiryResponse.class);

                } catch (HttpClientErrorException.NotFound e) {
                        log.error("포트원에서 결제 정보를 찾을 수 없습니다. impUid={}", impUid);
                        throw new RuntimeException("결제 정보 없음 (포트원)", e);
                }

        }

        @Transactional
        public void verifyPaidAmountAndProcessPayment(WebhookRequest webhookRequest,
                        SingleInquiryResponse.ResponseData paymentData) {

                // 1. 주문 조회
                Order order = orderRepository.findByMerchantUid(webhookRequest.getMerchantUid())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "주문을 찾을 수 없습니다. MerchantUid: " + webhookRequest.getMerchantUid()));

                // 2. 금액 검증
                BigDecimal dbPaidAmount = order.getGrandAmount();
                BigDecimal actualPaidAmount = paymentData.getAmount();

                if (actualPaidAmount.compareTo(dbPaidAmount) != 0) {
                        log.error("위변조 감지! 주문금액: {}, 결제금액: {}", dbPaidAmount, actualPaidAmount);
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
                                orderService.cancelOrderInDB(order.getOrderId()); // 단순 상태 변경이 아닌, 재고/매출 취소 로직 전체 수행
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
                        sseEmitterManager.broadcast(message);
                } catch (Exception e) {
                        log.error("알림 전송 실패 (결제 로직은 성공함)", e);
                }
        }

        private void createPayment(Order order) {
                final String paymentId = "payment-" + UUID.randomUUID().toString();
                Payment payment = Payment.builder()
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