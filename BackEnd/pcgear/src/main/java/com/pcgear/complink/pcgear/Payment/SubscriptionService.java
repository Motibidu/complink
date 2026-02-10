package com.pcgear.complink.pcgear.Payment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.pcgear.complink.pcgear.Payment.model.SubscriptionRequest;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionService {

        private final RestClient restClient; // WebClient 대신 RestClient 사용
        private final SubscriptionRepository subscriptionRepository;
        private final PortoneProperties portoneProperties;

        public Subscription scheduleNextPayment(UserEntity user, SubscriptionRequest request, int seconds) {
                log.info("결제 예약 시작 ==========================================================");

                final String paymentId = "payment-" + UUID.randomUUID().toString();
                final String uri = String.format("/payments/%s/schedule", paymentId);

                // 다음 결제일 설정
                LocalDateTime nextPaymentTime = LocalDateTime.now().plusSeconds(seconds);
                String formattedNextPaymentTime = nextPaymentTime.atZone(ZoneId.of("Asia/Seoul"))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

                String trackingId = UUID.randomUUID().toString();
                String orderName = "정기결제 #" + trackingId;

                // 1. 요청 바디 구성
                Map<String, Object> requestBody = Map.of(
                                "payment", Map.of(
                                                "billingKey", request.getBillingKey(),
                                                "orderName", orderName,
                                                "customer", Map.of("id", user.getUsername()),
                                                "amount", Map.of("total", request.getAmount()),
                                                "currency", "KRW"),
                                "timeToPay", formattedNextPaymentTime);

                try {
                        // 2. RestClient 호출 (동기 방식)
                        restClient.post()
                                        .uri(portoneProperties.getApiUrl() + uri)
                                        .header("Authorization", "PortOne " + portoneProperties.getApiSecret())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(requestBody)
                                        .retrieve()
                                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                                        (req, res) -> {
                                                                String errorBody = new String(
                                                                                res.getBody().readAllBytes());
                                                                log.error("정기결제 예약 실패: {}", errorBody);
                                                                throw new RuntimeException(
                                                                                "PortOne API 에러: " + errorBody);
                                                        })
                                        .toBodilessEntity(); // 응답 본문 무시

                        log.info("사용자 ID {}의 결제 예약 성공 (결제ID: {})", user.getUsername(), paymentId);

                        // 3. DB 저장 (동기 실행)
                        Subscription subscription = new Subscription();
                        subscription.setUserId(user.getUsername());
                        subscription.setAmount(request.getAmount());
                        subscription.setBillingKey(request.getBillingKey());
                        subscription.setStatus("ACTIVE");
                        subscription.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
                        subscription.setNextBillingTime(nextPaymentTime);
                        subscription.setTrackingId(trackingId);
                        subscription.setOrderName(orderName);

                        return subscriptionRepository.save(subscription);

                } catch (Exception e) {
                        log.error("결제 예약 처리 중 오류 발생: {}", e.getMessage());
                        throw e;
                }
        }

        // 웹훅 처리 로직
        public void processSubscriptionPayment(Map<String, Object> paymentDetail) {
                String orderName = (String) paymentDetail.get("orderName");
                String trackingId = extractTrackingIdFromOrderName(orderName);

                Subscription subscription = subscriptionRepository.findByTrackingId(trackingId)
                                .orElseThrow(() -> new EntityNotFoundException("구독 정보 없음: " + trackingId));

                log.info("구독 결제 승인 완료 처리: {}", trackingId);
                // 추가 로직...
        }

        private String extractTrackingIdFromOrderName(String orderName) {
                if (orderName == null || !orderName.contains("#"))
                        return null;
                return orderName.substring(orderName.lastIndexOf('#') + 1).trim();
        }
}