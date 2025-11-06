package com.pcgear.complink.pcgear.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkRequest;
import com.pcgear.complink.pcgear.Payment.model.PaymentLinkResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Slf4j
@Service
public class PaymentLinkService {

        private final WebClient webClient;
        private final ObjectMapper objectMapper;

        private final String webhookUrl;

        private static final String CREATE_PAYMENT_LINK_URI = "https://api.iamport.co/api/supplements/v1/link/payment";

        public PaymentLinkService(WebClient.Builder webClientBuilder,
                        ObjectMapper objectMapper, @Value("${portone.webhook-url}") String webhookUrl) {
                this.webClient = webClientBuilder.build();
                this.objectMapper = objectMapper;
                this.webhookUrl = webhookUrl;
        }

        public String createPaymentLink(String merchantUid, int amount, String productName, String buyerTel) {

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

                PaymentLinkResponse response = webClient.post()
                                .uri(CREATE_PAYMENT_LINK_URI)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(PaymentLinkResponse.class)
                                .block(); // 👈 Mono가 결과를 반환할 때까지 여기서 대기합니다.
                try {
                        System.out.println("response JSON: " + objectMapper.writeValueAsString(response));
                } catch (JsonProcessingException e) {
                        System.err.println("response 객체 JSON 직렬화 실패: " + e.getMessage());
                        System.out.println("response (toString fallback): " + response.toString());
                }

                // DTO가 바로 shortenedUrl을 가지고 있으므로, null 체크 후 바로 반환
                if (response != null && response.getShortenedUrl() != null) {
                        return response.getShortenedUrl(); // 성공 시 단축 URL 반환
                } else {
                        throw new RuntimeException("결제 링크 생성 실패: 응답에서 shortenedUrl을 찾을 수 없습니다.");
                }
        }
}