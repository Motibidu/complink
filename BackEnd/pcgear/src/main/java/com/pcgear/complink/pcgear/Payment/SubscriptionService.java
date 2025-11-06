package com.pcgear.complink.pcgear.Payment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.pcgear.complink.pcgear.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionService {

        private final WebClient webClient;
        private final SubscriptionRepository subscriptionRepository;
        private final PortoneProperties portoneProperties;

        public Mono<Subscription> scheduleNextPayment(UserEntity user, SubscriptionRequest request, int seconds) {
                log.info("결제예약==========================================================");

                final String paymentId = "payment-" + UUID.randomUUID().toString();
                // API 경로: /payments/{payment_id}/schedule
                final String uri = portoneProperties.getApiUrl() + String.format("/payments/%s/schedule", paymentId);

                // 다음 결제일 설정
                LocalDateTime nextPaymentTime = LocalDateTime.now().plusSeconds(seconds);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                String formattedNextPaymentTime = nextPaymentTime.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
                log.info("formattedNextPaymentTime: {}", formattedNextPaymentTime);

                String trackingId = UUID.randomUUID().toString();
                String orderName = "정기결제 #" + trackingId;

                Subscription newSubscription = new Subscription();
                newSubscription.setUserId(user.getUsername());
                newSubscription.setBillingKey(request.getBillingKey());
                newSubscription.setStatus("PENDING_SCHEDULE"); // 상태는 예약 대기로 시작
                newSubscription.setAmount(request.getAmount());
                newSubscription.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
                newSubscription.setNextBillingTime(nextPaymentTime);
                newSubscription.setTrackingId(trackingId);
                newSubscription.setOrderName(orderName);

                subscriptionRepository.save(newSubscription);

                Map<String, Object> paymentData = Map.of(
                                "billingKey", request.getBillingKey(),
                                "orderName", orderName,
                                "customer", createCustomerMap(user),
                                "amount", Map.of("total", request.getAmount()),
                                "currency", "KRW");

                Map<String, Object> requestBody = Map.of(
                                "payment", paymentData,
                                "timeToPay", formattedNextPaymentTime);

                // 1. WebClient 호출 (비동기)
                return webClient.post()
                                .uri(uri)
                                .header("Authorization", "PortOne "
                                                + portoneProperties.getApiSecret())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(requestBody)
                                .retrieve()
                                // 2. HTTP 4xx/5xx 오류 처리 (RestTemplate의 try-catch 역할)
                                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                                response -> response.bodyToMono(String.class).flatMap(body -> {
                                                        log.error("정기결제 예약 실패 (사용자 ID: {}): 상태코드 - {}, 응답 - {}",
                                                                        user.getUsername(), response.statusCode(),
                                                                        body);
                                                        return Mono.error(new RuntimeException("정기결제 예약 실패: " + body));
                                                }))
                                // 3. 성공 응답 처리 (String 본문을 소비하고 Mono<Subscription>으로 변환)
                                // .bodyToMono(String.class) // 응답 본문을 사용할 경우
                                .bodyToMono(Void.class) // 응답 본문이 필요 없다면 Void.class로 비우는 것이 효율적
                                .then(Mono.defer(() -> {
                                        // 4. API 호출 성공 시 DB에 Subscription 저장
                                        log.info("사용자 ID {}의 다음 정기결제가 성공적으로 예약되었습니다. (결제ID: {})",
                                                        user.getUsername(), paymentId); // ⭐️ 이제 이 로그가 출력됩니다.

                                        Subscription subscription = new Subscription();
                                        subscription.setUserId(user.getUsername());
                                        subscription.setAmount(request.getAmount());
                                        subscription.setBillingKey(request.getBillingKey());
                                        subscription.setStatus("ACTIVE");
                                        subscription.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
                                        subscription.setNextBillingTime(nextPaymentTime);

                                        // 5. DB 저장은 블로킹 작업이므로 Mono.fromCallable로 감싸서 반환
                                        return Mono.fromCallable(() -> subscriptionRepository.save(subscription))
                                                        .subscribeOn(Schedulers.boundedElastic());
                                }));
        }

        // createCustomerMap 헬퍼 메서드 (가정)
        private Map<String, String> createCustomerMap(UserEntity user) {
                return Map.of("id", user.getUsername());
        }
}
