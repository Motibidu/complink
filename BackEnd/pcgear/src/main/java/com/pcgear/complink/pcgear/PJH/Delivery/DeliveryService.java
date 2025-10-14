package com.pcgear.complink.pcgear.PJH.Delivery;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.pcgear.complink.pcgear.PJH.Customer.Customer;
import com.pcgear.complink.pcgear.PJH.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.PJH.Delivery.model.AccessTokenResp;
import com.pcgear.complink.pcgear.PJH.Delivery.model.Delivery;
import com.pcgear.complink.pcgear.PJH.Delivery.model.GraphQLRequest;
import com.pcgear.complink.pcgear.PJH.Delivery.model.RegisterWebhookResp;
import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.PJH.Delivery.model.ValidationResult;

import jakarta.persistence.EntityNotFoundException;

import com.pcgear.complink.pcgear.PJH.Delivery.model.RegisterWebhookInput;
import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {
        private final WebClient webClient;
        private final CustomerRepository customerRepository;
        private final DeliveryRepository deliveryRepository;
        private static final String GRAPHQL_API_URL = "https://apis.tracker.delivery/graphql";
        private static final String TRACK_DELIVERY_QUERY = """
                                                    query Track(
                        $carrierId: ID!,
                        $trackingNumber: String!
                        ) {
                        track(
                          carrierId: $carrierId,
                          trackingNumber: $trackingNumber
                        ) {
                          lastEvent {
                            time
                            status {
                              code
                              name
                            }
                            description
                          }
                          events(last: 10) {
                            edges {
                              node {
                                time
                                status {
                                  code
                                  name
                                }
                                description
                              }
                            }
                          }
                        }
                        }
                                                """;

        public String getAccessToken(TrackingNumberReq waybillReq) {
                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("grant_type", "client_credentials");
                formData.add("client_id", "AA6mQRHNYCMkBhwhlZ0A1nyy");
                formData.add("client_secret", "2qr3KjgSD6v1woHpiBbdhvEfcayAtw9FoHXfqQ7RZzK");

                AccessTokenResp accessTokenResp = webClient.post()
                                .uri("https://auth.tracker.delivery/oauth2/token")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .bodyValue(formData)
                                .retrieve()
                                .bodyToMono(AccessTokenResp.class).block();
                String accessToken = accessTokenResp.getAccess_token();

                return accessToken;
        }

        public Mono<ValidationResult> registerWebhookIfValid(String accessToken, TrackingNumberReq trackingNumberReq,
                        String myCallbackUrl) {
                return isValidDelivery(trackingNumberReq,
                                accessToken)
                                .flatMap(result -> {
                                        if (result.isValid()) {
                                                log.info("Delivery is valid. Proceeding to register webhook.");
                                                return registerWebhook(accessToken, trackingNumberReq.getCarrierId(),
                                                                trackingNumberReq.getTrackingNumber(),
                                                                myCallbackUrl);
                                        } else {
                                                log.warn("Delivery is NOT valid. Skipping webhook registration. Reason: {}",
                                                                result.getMessage());
                                                return Mono.just(new ValidationResult(false, result.getMessage()));
                                        }
                                });
        }

        public Mono<ValidationResult> registerWebhook(String accessToken, String carrierId, String trackingNumber,
                        String myCallbackUrl) {
                // 1. 실제 파라미터 객체
                RegisterWebhookInput registerWebhookInput = RegisterWebhookInput.builder()
                                .carrierId(carrierId)
                                .trackingNumber(trackingNumber)
                                .callbackUrl(myCallbackUrl)
                                .expirationTime(getExpirationTime(48)) // ISO 8601 형식
                                .build();
                log.info("registerWebhookInput: {}", registerWebhookInput);

                // 2. GraphQL 요청 본문 객체 생성
                GraphQLRequest requestBody = GraphQLRequest.builder()
                                .query("""
                                                                            mutation RegisterTrackWebhook($input: RegisterTrackWebhookInput!) {
                                                    registerTrackWebhook(input: $input)
                                                }
                                                                                                                    """)
                                // variables 필드에 Map 형태로 input 객체 삽입
                                .variables(new java.util.HashMap<String, RegisterWebhookInput>() {
                                        {
                                                put("input", registerWebhookInput);
                                        }
                                })
                                .build();

                // 3. WebClient 호출
                return webClient.post()
                                .uri(GRAPHQL_API_URL) // GraphQL API URL
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // Bearer 토큰 인증
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(requestBody) // requestBody 객체가 JSON으로 변환되어 전송됨
                                .retrieve()
                                .bodyToMono(RegisterWebhookResp.class)
                                .map(response -> {
                                        log.info("response: {}", response);
                                        // 응답 본문에서 data.registerTrackWebhook이 true인지 확인
                                        if (response.getData() != null && response.getData()
                                                        .getRegisterTrackWebhook() == Boolean.TRUE) {
                                                return new ValidationResult(true, "추적 등록을 완료했습니다.");
                                        } else {
                                                // API는 성공했지만, 등록 결과가 true가 아닌 경우 (false 또는 null)
                                                return new ValidationResult(false, "추적 등록에 실패하였습니다.");
                                        }
                                });
        }

        public Mono<TrackingResponse> trackDelivery(String carrierId, String trackingNumber, String accessToken) {

                Map<String, String> variables = Map.of(
                                "carrierId", carrierId,
                                "trackingNumber", trackingNumber);

                GraphQLRequest requestBody = GraphQLRequest.builder()
                                .query(TRACK_DELIVERY_QUERY)
                                .variables(variables)
                                .build();

                return webClient.post()
                                .uri(GRAPHQL_API_URL)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(TrackingResponse.class);
        }

        public Mono<ValidationResult> isValidDelivery(TrackingNumberReq trackingNumberReq,
                        String accessToken) {
                return this.trackDelivery(trackingNumberReq.getCarrierId(), trackingNumberReq.getTrackingNumber(),
                                accessToken)
                                .map(response -> {
                                        // 1. GraphQL 오류 응답이 있거나, data.track이 null이면 유효하지 않음 (NOT_FOUND 포함)
                                        if ((response.getErrors() != null && !response.getErrors().isEmpty()) ||
                                                        (response.getData() == null
                                                                        || response.getData().getTrack() == null)) {
                                                String errorMessage = response.getErrors().get(0).getMessage();
                                                if (errorMessage.isEmpty()) {
                                                        // 유효하지 않은 운송장 번호일 때 (메시지: "")
                                                        return new ValidationResult(false, "운송장 번호를 찾지 못했습니다.");
                                                } else if ("Carrier not found".equals(errorMessage)) {
                                                        // 유효하지 않은 택배사 코드일 때 (메시지: "Carrier not found")
                                                        return new ValidationResult(false, "택배사 코드를 찾지 못했습니다.");
                                                } else {
                                                        // 그 외 알 수 없는 GraphQL 오류
                                                        return new ValidationResult(false,
                                                                        "알 수 없는 배송 조회 오류가 발생했습니다: " + errorMessage);
                                                }
                                        }
                                        // createDelivery();
                                        Delivery delivery = new Delivery();
                                        delivery.setCarrierId(trackingNumberReq.getCarrierId());
                                        delivery.setTrackingNumber(trackingNumberReq.getTrackingNumber());
                                        delivery.setOrderId(trackingNumberReq.getOrderId());
                                        Customer customer = customerRepository
                                                        .findById(trackingNumberReq.getCustomerId())
                                                        .orElseThrow(() -> new EntityNotFoundException(
                                                                        "해당 ID의 거래처를 찾을 수 없습니다." + trackingNumberReq
                                                                                        .getCustomerId()));
                                        delivery.setCustomerId(trackingNumberReq.getCustomerId());
                                        delivery.setRecipientName(customer.getCustomerName());
                                        delivery.setRecipientPhone(customer.getPhoneNumber());
                                        delivery.setRecipientAddr(customer.getAddress());
                                        deliveryRepository.save(delivery);
                                        return new ValidationResult(true, "배송 조회에 성공했습니다.");
                                });
        }

        private String getExpirationTime(int hoursLater) {
                // UTC 시간 기준으로 현재 시간 + 지정된 시간
                return OffsetDateTime.now(ZoneOffset.UTC)
                                .plusHours(hoursLater)
                                .format(DateTimeFormatter.ISO_INSTANT); // ISO 8601 형식
        }

}
