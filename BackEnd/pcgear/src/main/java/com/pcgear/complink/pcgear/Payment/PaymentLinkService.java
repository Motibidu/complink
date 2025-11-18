package com.pcgear.complink.pcgear.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkRequest;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkResponse;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1AccessTokenReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1AccessTokenResp;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelReq;
import com.pcgear.complink.pcgear.Payment.model.PortoneV1CancelResp;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Slf4j
@Service
public class PaymentLinkService {

        private final WebClient webClient;
        private final ObjectMapper objectMapper;
        private final PortoneProperties portoneProperties;
        private final OrderRepository orderRepository;

        private final String webhookUrl;

        private static final String CREATE_PAYMENT_LINK_URI = "https://api.iamport.co/api/supplements/v1/link/payment";
        private static final String PORTONE_V1_GET_ACCESS_TOKEN_URI = "https://api.iamport.kr/users/getToken";
        private static final String PORTONE_V1_CANCEL_PAYMENT_URI = "https://api.iamport.kr/payments/cancel";

        public PaymentLinkService(WebClient.Builder webClientBuilder,
                        ObjectMapper objectMapper, @Value("${portone.webhook-url}") String webhookUrl,
                        PortoneProperties portoneProperties, OrderRepository orderRepository) {
                this.webClient = webClientBuilder.build();
                this.objectMapper = objectMapper;
                this.webhookUrl = webhookUrl;
                this.portoneProperties = portoneProperties;
                this.orderRepository = orderRepository;
        }

        public Mono<String> createPaymentLink(String merchantUid, int amount, String productName, String buyerTel) {

                log.info("portone.webhook-url: {}", this.webhookUrl);

                // 1. payment_info에 들어갈 내부 객체 생성
                PaymentLinkRequest.PaymentInfo.PayMethod cardPayMethod = PaymentLinkRequest.PaymentInfo.PayMethod
                                .builder()
                                .pg("tosspay_v2") // 실제 사용할 PG사 코드로 변경
                                .pay_method("tosspay_money")
                                .label("토스페이")
                                .build();

                PaymentLinkRequest.PaymentInfo paymentInfo = PaymentLinkRequest.PaymentInfo.builder()
                                .title("PCGear 주문 결제")
                                .user_code("imp38514028")
                                .amount(amount)
                                .merchant_uid(merchantUid)
                                .name(productName)
                                .currency("KRW")
                                .buyer_tel(buyerTel)
                                .notice_url(this.webhookUrl + "/payment/webhook/verify/paymentLink")
                                .pay_methods(Collections.singletonList(cardPayMethod))
                                .build();

                // 2. PaymentInfo 객체를 JSON 문자열로 변환
                String paymentInfoJsonString;
                try {
                        paymentInfoJsonString = objectMapper.writeValueAsString(paymentInfo);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException("PaymentInfo 객체 JSON 직렬화 실패", e);
                }

                // 3. 최종 요청 본문 DTO 생성
                long expiredAt = Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond();

                PaymentLinkRequest requestBody = new PaymentLinkRequest();
                requestBody.setPaymentInfo(paymentInfoJsonString);
                requestBody.setExpired_at(expiredAt);

                try {
                        System.out.println("requestBody JSON: " + objectMapper.writeValueAsString(requestBody));
                } catch (JsonProcessingException e) {
                        System.err.println("requestBody 객체 JSON 직렬화 실패: " + e.getMessage());
                        System.out.println("requestBody (toString fallback): " + requestBody.toString());
                }

                return webClient.post()
                                .uri(CREATE_PAYMENT_LINK_URI)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(PaymentLinkResponse.class)
                                // map을 사용하여 결과 변환 (여기서 예외 처리도 가능)
                                .map(response -> {
                                        if (response != null && response.getShortenedUrl() != null) {
                                                return response.getShortenedUrl();
                                        } else {
                                                throw new RuntimeException("결제 링크 생성 실패: 응답 없음");
                                        }
                                })
                                // 로그는 부가 효과(side-effect)로 처리
                                .doOnNext(url -> log.info("생성된 단축 URL: {}", url));
        }

        // 포트원 v1 결제취소
        public Mono<PortoneV1CancelResp> cancelPayment(Integer orderId, String reason) {

                // 1. 토큰을 먼저 받아옴
                return getAccessToken()
                                .flatMap(accessToken -> {
                                        Order order = orderRepository.findById(orderId).orElseThrow(
                                                        () -> new EntityNotFoundException(
                                                                        "주문 정보를 찾을 수 없습니다." + orderId));
                                        String impUid = order.getImpUid();

                                        PortoneV1CancelReq request = new PortoneV1CancelReq(impUid, reason);

                                        return webClient.post()
                                                        .uri(PORTONE_V1_CANCEL_PAYMENT_URI) // 결제 취소 URL
                                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // 인증
                                                        .bodyValue(request)
                                                        .retrieve()
                                                        .bodyToMono(PortoneV1CancelResp.class)
                                                        .doOnNext(response -> {
                                                                if (response.getCode() != 0) { // 0이 아니면 실패
                                                                        log.error("포트원 결제 취소 실패: {}",
                                                                                        response.getMessage());
                                                                }
                                                        });
                                });
        }

        // 포트원 v1 인증토큰 발급
        private Mono<String> getAccessToken() {

                PortoneV1AccessTokenReq request = new PortoneV1AccessTokenReq(portoneProperties.getImpKey(),
                                portoneProperties.getImpSecret());

                return webClient.post()
                                .uri(PORTONE_V1_GET_ACCESS_TOKEN_URI) // 토큰 발급 URL
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(PortoneV1AccessTokenResp.class)
                                .doOnNext(responseDto -> {
                                        log.info("Full API Response DTO: {}", responseDto);

                                        // 내부 response 객체나 토큰만 따로 볼 수도 있습니다.
                                        if (responseDto.getResponse() != null) {
                                                log.info("Response Data part: {}",
                                                                responseDto.getResponse().getAccess_token());
                                        }
                                })
                                .map(response -> response.getResponse().getAccess_token()); // 토큰 문자열만 추출
        }

        // 포트원 v1 결제링크 취소
        public Mono<String> cancelPaymentLink(String paymentLink) {
                String linkId = paymentLink.substring(paymentLink.lastIndexOf("/") + 1);
                return webClient.put()
                                .uri("https://api.iamport.co/api/supplements/v1/link/payment/" + linkId)
                                .retrieve()
                                .bodyToMono(String.class) // 1. 일단 String으로 받음 ("{}")
                                .map(response -> {
                                        return "결제 링크가 성공적으로 만료(취소)되었습니다.";
                                })
                                .doOnError(e -> log.error("링크 만료 실패: {}", e.getMessage()));
        }

}