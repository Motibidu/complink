package com.pcgear.complink.pcgear.Payment;

import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Payment.model.AccessTokenResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkRequest;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkResponse;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1AccessTokenReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelResp;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLinkService {

        private final RestClient restClient;
        private final PortoneProperties portoneProperties;
        private final OrderRepository orderRepository;

        // private final String webhookUrl;

        private static final String CREATE_PAYMENT_LINK_URI = "https://api.iamport.co/api/supplements/v1/link/payment";
        private static final String PORTONE_V1_GET_ACCESS_TOKEN_URI = "https://api.iamport.kr/users/getToken";
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
                                .user_code(portoneProperties.getImpUserCode()) // properties로 관리 추천
                                .amount(amount)
                                .merchant_uid(merchantUid)
                                .name(productName)
                                .currency("KRW")
                                .buyer_tel(buyerTel)
                                .notice_url(portoneProperties.getWebhookUrl() + "/payment/webhook/verify/paymentLink")
                                .pay_methods(Collections.singletonList(cardPayMethod))
                                .build();

                // 2. 최종 요청 본문 생성
                String paymentInfoJsonString;
                try {
                        paymentInfoJsonString = new com.fasterxml.jackson.databind.ObjectMapper()
                                        .writeValueAsString(paymentInfo);
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
                // 1. 토큰 발급 (동기 호출)
                String accessToken = getAccessToken();

                // 2. 주문 조회 (DB)
                String impUid = orderRepository.findById(orderId)
                                .map(Order::getImpUid)
                                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

                PortoneV1CancelReq request = new PortoneV1CancelReq(impUid, reason);

                try {
                        // 3. API 호출
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

        public PortoneV1CancelResp cancelPayment(String accessToken, String impUid, String reason) {

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
                                        .uri(PORTONE_V1_GET_ACCESS_TOKEN_URI)
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

}