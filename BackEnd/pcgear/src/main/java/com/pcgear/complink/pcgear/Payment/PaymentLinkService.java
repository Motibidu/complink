package com.pcgear.complink.pcgear.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Payment.model.AccessTokenResponse;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkRequest;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkResponse;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1AccessTokenReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1AccessTokenResp;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelResp;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLinkService {

        private final ObjectMapper objectMapper;
        private final RestClient restClient;
        private final PortoneProperties portoneProperties;
        private final OrderRepository orderRepository;

        // private final String webhookUrl;

        private static final String CREATE_PAYMENT_LINK_URI = "https://api.iamport.co/api/supplements/v1/link/payment";
        private static final String PORTONE_V1_GET_ACCESS_TOKEN_URI = "https://api.iamport.kr/users/getToken";
        private static final String PORTONE_V1_CANCEL_PAYMENT_URI = "https://api.iamport.kr/payments/cancel";

        // public PaymentLinkService(WebClient.Builder webClientBuilder,
        // ObjectMapper objectMapper, @Value("${portone.webhook-url}") String
        // webhookUrl,
        // PortoneProperties portoneProperties, OrderRepository orderRepository) {
        // this.objectMapper = objectMapper;
        // this.webhookUrl = webhookUrl;
        // this.portoneProperties = portoneProperties;
        // this.orderRepository = orderRepository;
        // }

        // public Mono<String> createPaymentLink(String merchantUid, int amount, String
        // productName, String buyerTel) {

        // log.info("portone.webhook-url: {}", this.webhookUrl);

        // // 1. payment_info에 들어갈 내부 객체 생성
        // PaymentLinkRequest.PaymentInfo.PayMethod cardPayMethod =
        // PaymentLinkRequest.PaymentInfo.PayMethod
        // .builder()
        // .pg("tosspay_v2") // 실제 사용할 PG사 코드로 변경
        // .pay_method("tosspay_money")
        // .label("토스페이")
        // .build();

        // PaymentLinkRequest.PaymentInfo paymentInfo =
        // PaymentLinkRequest.PaymentInfo.builder()
        // .title("PCGear 주문 결제")
        // .user_code("imp38514028")
        // .amount(amount)
        // .merchant_uid(merchantUid)
        // .name(productName)
        // .currency("KRW")
        // .buyer_tel(buyerTel)
        // .notice_url(this.webhookUrl + "/payment/webhook/verify/paymentLink")
        // .pay_methods(Collections.singletonList(cardPayMethod))
        // .build();

        // // 2. PaymentInfo 객체를 JSON 문자열로 변환
        // String paymentInfoJsonString;
        // try {
        // paymentInfoJsonString = objectMapper.writeValueAsString(paymentInfo);
        // } catch (JsonProcessingException e) {
        // throw new RuntimeException("PaymentInfo 객체 JSON 직렬화 실패", e);
        // }

        // // 3. 최종 요청 본문 DTO 생성
        // long expiredAt = Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond();

        // PaymentLinkRequest requestBody = new PaymentLinkRequest();
        // requestBody.setPaymentInfo(paymentInfoJsonString);
        // requestBody.setExpired_at(expiredAt);

        // try {
        // System.out.println("requestBody JSON: " +
        // objectMapper.writeValueAsString(requestBody));
        // } catch (JsonProcessingException e) {
        // System.err.println("requestBody 객체 JSON 직렬화 실패: " + e.getMessage());
        // System.out.println("requestBody (toString fallback): " +
        // requestBody.toString());
        // }

        // return webClient.post()
        // .uri(CREATE_PAYMENT_LINK_URI)
        // .contentType(MediaType.APPLICATION_JSON)
        // .bodyValue(requestBody)
        // .retrieve()
        // .bodyToMono(PaymentLinkResponse.class)
        // // map을 사용하여 결과 변환 (여기서 예외 처리도 가능)
        // .map(response -> {
        // if (response != null && response.getShortenedUrl() != null) {
        // return response.getShortenedUrl();
        // } else {
        // throw new RuntimeException("결제 링크 생성 실패: 응답 없음");
        // }
        // })
        // // 로그는 부가 효과(side-effect)로 처리
        // .doOnNext(url -> log.info("생성된 단축 URL: {}", url));
        // }

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
                                .user_code("imp38514028") // properties로 관리 추천
                                .amount(amount)
                                .merchant_uid(merchantUid)
                                .name(productName)
                                .currency("KRW")
                                .buyer_tel(buyerTel)
                                .notice_url(portoneProperties.getWebhookUrl() + "/payment/webhook/verify/paymentLink")
                                .pay_methods(Collections.singletonList(cardPayMethod))
                                .build();

                // 2. 최종 요청 본문 생성
                // (주의: 포트원 V1 링크 API는 payment_info를 "JSON 문자열"로 요구하는 특이한 구조임.
                // RestClient가 객체 -> JSON 변환은 해주지만, 이중 직렬화는 수동 처리가 필요할 수 있음.
                // 하지만 여기서는 DTO 필드가 String으로 선언되어 있다고 가정하고 진행하거나,
                // 만약 객체 자체를 받는다면 그대로 넣으면 됨. *여기서는 DTO 구조에 맞게 객체를 넣음*)

                // **[중요 체크]** PaymentLinkRequest의 paymentInfo 필드가 String 타입이라면
                // ObjectMapper로 한 번 변환해서 넣어야 함. (기존 코드 로직 유지)
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

        // 포트원 v1 결제취소
        // public Mono<PortoneV1CancelResp> cancelPayment(Integer orderId, String
        // reason) {

        // // 1. 토큰을 먼저 받아옴
        // return getAccessToken()
        // .flatMap(accessToken -> {
        // Order order = orderRepository.findById(orderId).orElseThrow(
        // () -> new EntityNotFoundException(
        // "주문 정보를 찾을 수 없습니다." + orderId));
        // String impUid = order.getImpUid();

        // PortoneV1CancelReq request = new PortoneV1CancelReq(impUid, reason);

        // return webClient.post()
        // .uri(PORTONE_V1_CANCEL_PAYMENT_URI) // 결제 취소 URL
        // .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // 인증
        // .bodyValue(request)
        // .retrieve()
        // .bodyToMono(PortoneV1CancelResp.class)
        // .doOnNext(response -> {
        // if (response.getCode() != 0) { // 0이 아니면 실패
        // log.error("포트원 결제 취소 실패: {}",
        // response.getMessage());
        // }
        // });
        // });
        // }

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

        // 포트원 v1 인증토큰 발급
        // private Mono<String> getAccessToken() {

        // PortoneV1AccessTokenReq request = new
        // PortoneV1AccessTokenReq(portoneProperties.getImpKey(),
        // portoneProperties.getImpSecret());

        // return webClient.post()
        // .uri(PORTONE_V1_GET_ACCESS_TOKEN_URI) // 토큰 발급 URL
        // .bodyValue(request)
        // .retrieve()
        // .bodyToMono(PortoneV1AccessTokenResp.class)
        // .doOnNext(responseDto -> {
        // log.info("Full API Response DTO: {}", responseDto);

        // // 내부 response 객체나 토큰만 따로 볼 수도 있습니다.
        // if (responseDto.getResponse() != null) {
        // log.info("Response Data part: {}",
        // responseDto.getResponse().getAccess_token());
        // }
        // })
        // .map(response -> response.getResponse().getAccess_token()); // 토큰 문자열만 추출
        // }
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

        // 포트원 v1 결제링크 취소
        // public Mono<String> cancelPaymentLink(String paymentLink) {
        // String linkId = paymentLink.substring(paymentLink.lastIndexOf("/") + 1);
        // return webClient.put()
        // .uri("https://api.iamport.co/api/supplements/v1/link/payment/" + linkId)
        // .retrieve()
        // .bodyToMono(String.class) // 1. 일단 String으로 받음 ("{}")
        // .map(response -> {
        // return "결제 링크가 성공적으로 만료(취소)되었습니다.";
        // })
        // .doOnError(e -> log.error("링크 만료 실패: {}", e.getMessage()));
        // }

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