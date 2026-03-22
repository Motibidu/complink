package com.pcgear.complink.pcgear.Delivery;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.pcgear.complink.pcgear.Delivery.exception.WebhookRegistrationException;
import com.pcgear.complink.pcgear.Delivery.model.RegisterWebhookInput;
import com.pcgear.complink.pcgear.Delivery.model.req.GraphQLReq;
import com.pcgear.complink.pcgear.Delivery.model.resp.RegisterWebhookResp;
import com.pcgear.complink.pcgear.properties.DeliveryTrackerProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹훅 등록 전담 서비스
 * - 웹훅 등록 API 호출
 * - 재시도 로직 (Spring @Retryable)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final RestClient restClient;
    private final DeliveryTrackerProperties properties;

    /**
     * 배송 추적 웹훅 등록
     * - Spring @Retryable: 네트워크 오류 시 최대 3회 자동 재시도 (1초 간격)
     * - 재시도 대상: SocketTimeoutException, ConnectException, ResourceAccessException
     * - Recover: 3회 모두 실패 시 webhookRegistrationFallback 메서드 호출
     *
     * @throws WebhookRegistrationException 웹훅 등록 실패 시
     */
    @Retryable(
        value = {
            java.net.SocketTimeoutException.class,
            java.net.ConnectException.class,
            org.springframework.web.client.ResourceAccessException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000),
        recover = "webhookRegistrationFallback"
    )
    public void registerWebhook(String accessToken, String carrierId, String trackingNumber, String callbackUrl) {
        log.info("웹훅 등록 시작: carrierId={}, trackingNumber={}", carrierId, trackingNumber);

        RegisterWebhookInput input = RegisterWebhookInput.builder()
                .carrierId(carrierId)
                .trackingNumber(trackingNumber)
                .callbackUrl(callbackUrl)
                .expirationTime(getExpirationTime(48))
                .build();

        GraphQLReq requestBody = GraphQLReq.builder()
                .query("mutation RegisterTrackWebhook($input: RegisterTrackWebhookInput!) { registerTrackWebhook(input: $input) }")
                .variables(Map.of("input", input))
                .build();

        RegisterWebhookResp response = restClient.post()
                .uri(properties.getGraphqlApiUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("API 응답 에러 코드: {}", res.getStatusCode());
                    log.error("API 응답 메세지: {}", res.getStatusText());
                    log.error("API 응답 바디: {}", new String(res.getBody().readAllBytes()));
                })
                .body(RegisterWebhookResp.class);

        log.info("웹훅 등록 응답: {}", response);

        if (response != null && response.getData() != null
                && Boolean.TRUE.equals(response.getData().getRegisterTrackWebhook())) {
            log.info("웹훅 등록 성공: carrierId={}, trackingNumber={}", carrierId, trackingNumber);
            return;
        }

        // 실패 시 예외 던지기
        String errorMessage = extractErrorMessage(response);
        log.error("웹훅 등록 실패: {}", errorMessage);
        throw new WebhookRegistrationException(errorMessage);
    }

    /**
     * 웹훅 등록 Recover 메서드
     * - @Retryable로 3회 재시도 모두 실패 시 자동 호출
     */
    @Recover
    private void webhookRegistrationFallback(Exception e, String accessToken, String carrierId,
            String trackingNumber, String callbackUrl) {
        log.error("웹훅 등록 3회 재시도 모두 실패. carrierId={}, trackingNumber={}, Error: {}",
                carrierId, trackingNumber, e.getMessage());

        throw new WebhookRegistrationException(
                "웹훅 등록 실패 (3회 재시도 완료): " + e.getMessage(), e);
    }

    private String extractErrorMessage(RegisterWebhookResp response) {
        if (response != null && response.getErrors() != null && !response.getErrors().isEmpty()) {
            return response.getErrors().get(0).getMessage();
        }
        return "웹훅 등록 실패 (응답 없음)";
    }

    private String getExpirationTime(int hoursLater) {
        Instant expirationInstant = Instant.now().plus(hoursLater, java.time.temporal.ChronoUnit.HOURS);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(java.time.ZoneOffset.UTC)
                .format(expirationInstant);
    }
}
