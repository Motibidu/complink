package com.pcgear.complink.pcgear.Delivery;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.model.AccessTokenResp;
import com.pcgear.complink.pcgear.Delivery.model.Delivery;
import com.pcgear.complink.pcgear.Delivery.model.GraphQLRequest;
import com.pcgear.complink.pcgear.Delivery.model.RegisterWebhookResp;
import com.pcgear.complink.pcgear.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.Delivery.model.WebhookReq;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.service.OrderService;

import jakarta.persistence.EntityNotFoundException;

import com.pcgear.complink.pcgear.Delivery.model.RegisterWebhookInput;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {
        private final WebClient webClient;
        private final OrderService orderService;
        private final CustomerRepository customerRepository;
        private final DeliveryRepository deliveryRepository;
        private final SimpMessagingTemplate messagingTemplate;
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

        public String getAccessToken() {
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

                return isValidDelivery(trackingNumberReq, accessToken)
                                .flatMap(validationResult -> {
                                        log.info("isValidDelivery: {}", validationResult);
                                        if (validationResult.isValid()) {
                                                // order의 status를 "배송 대기"로 변경
                                                orderService.updateOrderStatus(trackingNumberReq.getOrderId(),
                                                                OrderStatus.SHIPPING_PENDING);

                                                return registerWebhook(accessToken,
                                                                trackingNumberReq.getCarrierId(),
                                                                trackingNumberReq.getTrackingNumber(),
                                                                myCallbackUrl);
                                        } else {
                                                return Mono.just(validationResult);
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

        // 배송조회
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

        // 전달 받은 운송장번호로 배송조회에 완료 하면 유효한 운송장번호
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
                                        createDelivery(trackingNumberReq);
                                        return new ValidationResult(true, "배송 조회에 성공했습니다.");
                                });
        }

        public String getDeliveryStatus(TrackingResponse trackingResponse) {
                String currentStatus = null;
                if (trackingResponse != null && trackingResponse.getData() != null &&
                                trackingResponse.getData().getTrack() != null &&
                                trackingResponse.getData().getTrack().getLastEvent() != null &&
                                trackingResponse.getData().getTrack().getLastEvent().getStatus() != null) {

                        // 최종 상태 코드를 추출하여 변수에 할당
                        currentStatus = trackingResponse.getData().getTrack().getLastEvent().getStatus()
                                        .getName();
                }
                return currentStatus;
        }

        public boolean existsByOrderId(Integer orderId) {
                return deliveryRepository.existsByOrderId(orderId);

        }

        public List<Delivery> updateDeiliveryStatus(WebhookReq webhookReq, String currentStatus) {
                String trackingNumber = webhookReq.getTrackingNumber();

                List<Delivery> deliveries = deliveryRepository.findAllByTrackingNumber(trackingNumber);

                if (deliveries.isEmpty()) {
                        log.warn("Delivery record not found for tracking number: {}", trackingNumber);
                        return List.of();
                }

                for (Delivery delivery : deliveries) {
                        delivery.setCurrentStatus(currentStatus);
                        String message = "주문번호: " + delivery.getOrderId() + "의 배송상태가 "
                                        + currentStatus + "로 변경되었습니다. 배송조회에서 확인해주세요!";
                        messagingTemplate.convertAndSend("/topic/notifications", message);

                        // 배송 상태가 집화처리일 시 order의 status 배송대기-> 배송중으로 변경
                        if ("집화처리".equals(currentStatus)) {
                                orderService.updateOrderStatus(delivery.getOrderId(), OrderStatus.SHIPPING);
                        }

                        // 배송 상태가 배송완료일 시 order의 status를 배송중-> 배송완료로 변경
                        if ("배송완료".equals(currentStatus)) {
                                orderService.updateOrderStatus(delivery.getOrderId(), OrderStatus.DELIVERED);
                        }
                }

                return deliveryRepository.saveAll(deliveries);
        }

        private Delivery createDelivery(TrackingNumberReq trackingNumberReq) {

                Customer customer = customerRepository
                                .findById(trackingNumberReq.getCustomerId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "해당 ID의 거래처를 찾을 수 없습니다." + trackingNumberReq
                                                                .getCustomerId()));
                Delivery delivery = Delivery.builder()
                                .carrierId(trackingNumberReq.getCarrierId())
                                .trackingNumber(trackingNumberReq.getTrackingNumber())
                                .orderId(trackingNumberReq.getOrderId())
                                .customerId(trackingNumberReq.getCustomerId())
                                .recipientName(customer.getCustomerName())
                                .recipientPhone(customer.getPhoneNumber())
                                .recipientAddr(customer.getAddress())
                                .build();
                return deliveryRepository.save(delivery);
        }

        private String getExpirationTime(int hoursLater) {
                // 항상 밀리초(SSS)를 포함하는 ISO 8601 형식의 UTC 시간 생성
                Instant expirationInstant = Instant.now().plus(hoursLater, java.time.temporal.ChronoUnit.HOURS);
                return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .withZone(java.time.ZoneOffset.UTC)
                                .format(expirationInstant);
        }

        public Optional<Delivery> findByOrderId(Integer orderId) {
                return deliveryRepository.findByOrderId(orderId);
        }

}
