package com.pcgear.complink.pcgear.Delivery;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import com.pcgear.complink.pcgear.config.SseEmitterManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.model.AccessTokenResp;
import com.pcgear.complink.pcgear.Delivery.model.DeliveryStatus;
import com.pcgear.complink.pcgear.Delivery.model.GraphQLRequest;
import com.pcgear.complink.pcgear.Delivery.model.RegisterWebhookResp;
import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.properties.DeliveryTrackerProperties;

import jakarta.persistence.EntityNotFoundException;

import com.pcgear.complink.pcgear.Delivery.model.RegisterWebhookInput;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {

        @Lazy
        @Autowired
        private DeliveryService self;
        private final OrderService orderService;
        private final ItemService itemService;

        private final CustomerRepository customerRepository;
        private final OrderRepository orderRepository;
        private final DeliveryRepository deliveryRepository;

        private final SseEmitterManager sseEmitterManager;
        private final RestClient restClient;
        private final DeliveryTrackerProperties properties;
        private static final String TRACK_DELIVERY_QUERY = """
                        query Track($carrierId: ID!, $trackingNumber: String!) {
                          track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
                            lastEvent {
                              time
                              status { code name }
                              description
                            }
                            events(last: 10) {
                              edges {
                                node {
                                  time
                                  status { code name }
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
                formData.add("client_id", properties.getClientId());
                formData.add("client_secret", properties.getClientSecret());

                try {
                        return restClient.post()
                                        .uri(properties.getAuthUrl())
                                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                        .body(formData)
                                        .retrieve()
                                        .body(AccessTokenResp.class) // 바로 객체 변환
                                        .getAccess_token();
                } catch (Exception e) {
                        log.error("Failed to get access token", e);
                        throw new RuntimeException("Failed to get access token", e);
                }
        }

        public ValidationResult registerWebhookIfValid(String accessToken, TrackingNumberReq trackingNumberReq) {
                log.info("배송 추적 등록 시작: {}", trackingNumberReq);

                try {
                        // A. 배송 조회 API 호출 (동기)
                        TrackingResponse trackingRes = trackDelivery(trackingNumberReq.getCarrierId(),
                                        trackingNumberReq.getTrackingNumber(), accessToken);

                        log.info("배송 조회 결과: {}", trackingRes);

                        // B. 조회 결과 검증
                        ValidationResult validationResult = validateTrackingResponse(trackingRes);
                        if (!validationResult.isValid()) {
                                return validationResult; // 실패 시 바로 리턴
                        }

                        log.info("properties.getWebhookUrl(): {}", properties.getWebhookUrl());

                        // C. 웹훅 등록 API 호출 (동기)
                        ValidationResult webhookResult = registerWebhook(accessToken,
                                        trackingNumberReq.getCarrierId(),
                                        trackingNumberReq.getTrackingNumber(),
                                        properties.getWebhookUrl() + "/delivery/webhook");

                        if (webhookResult.isValid()) {
                                // D. [트랜잭션] 모든 API 성공 시 DB 저장 수행
                                self.processRegistrationTransaction(trackingNumberReq);
                                return new ValidationResult(true, "추적 등록 및 DB 반영 완료");
                        } else {
                                return webhookResult;
                        }

                } catch (Exception e) {
                        log.error("배송 추적 등록 프로세스 실패", e);
                        return new ValidationResult(false, "시스템 오류: " + e.getMessage());
                }
        }

        public TrackingResponse trackDelivery(String carrierId, String trackingNumber, String accessToken) {
                Map<String, String> variables = Map.of(
                                "carrierId", carrierId,
                                "trackingNumber", trackingNumber);

                GraphQLRequest requestBody = GraphQLRequest.builder()
                                .query(TRACK_DELIVERY_QUERY)
                                .variables(variables)
                                .build();

                return restClient.post()
                                .uri(properties.getGraphqlApiUrl())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(requestBody)
                                .retrieve()
                                .body(TrackingResponse.class);
        }

        private ValidationResult validateTrackingResponse(TrackingResponse response) {
                if (response == null || response.getData() == null || response.getData().getTrack() == null) {
                        String msg = "운송장 번호를 찾지 못했습니다.";
                        if (response != null && response.getErrors() != null && !response.getErrors().isEmpty()) {
                                String errorMsg = response.getErrors().get(0).getMessage();
                                if ("Carrier not found".equals(errorMsg))
                                        msg = "택배사 코드를 찾지 못했습니다.";
                                else if (!errorMsg.isEmpty())
                                        msg = "조회 오류: " + errorMsg;
                        }
                        return new ValidationResult(false, msg);
                }
                return new ValidationResult(true, "성공");
        }

        public ValidationResult registerWebhook(String accessToken, String carrierId, String trackingNumber,
                        String myCallbackUrl) {
                RegisterWebhookInput input = RegisterWebhookInput.builder()
                                .carrierId(carrierId)
                                .trackingNumber(trackingNumber)
                                .callbackUrl(myCallbackUrl)
                                .expirationTime(getExpirationTime(48))
                                .build();

                GraphQLRequest requestBody = GraphQLRequest.builder()
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
                log.info("response: {}", response);

                if (response != null && response.getData() != null
                                && Boolean.TRUE.equals(response.getData().getRegisterTrackWebhook())) {
                        return new ValidationResult(true, "추적 등록 완료");
                }

                String errorMsg = "추적 등록 실패";
                if (response != null && response.getErrors() != null && !response.getErrors().isEmpty()) {
                        errorMsg = "추적 등록 실패: " + response.getErrors().get(0).getMessage();
                }
                log.error("웹훅 등록 실패 - response: {}", response);
                return new ValidationResult(false, errorMsg);
        }

        @Transactional
        public void processRegistrationTransaction(TrackingNumberReq req) {
                // 배송 정보 생성
                createDelivery(req);

                // 주문 상태 변경 (배송 대기)
                orderService.updateOrderStatus(req.getOrderId(), OrderStatus.SHIPPING_PENDING);

                // 재고 차감
                Order order = orderRepository.findById(req.getOrderId())
                                .orElseThrow(() -> new EntityNotFoundException("주문 없음: " + req.getOrderId()));
                itemService.updateItemQuantityOnHand(order);

                log.info("배송 등록 트랜잭션 완료");
        }

        
        public String extractDeliveryStatus(TrackingResponse trackingResponse) {
                return Optional.ofNullable(trackingResponse)
                                .map(TrackingResponse::getData)
                                .map(TrackingResponse.TrackingData::getTrack)
                                .map(TrackingResponse.Track::getLastEvent)
                                .map(TrackingResponse.LastEvent::getStatus)
                                .map(TrackingResponse.EventStatus::getName)
                                .orElse(null);
        }

        @Transactional
        public Delivery updateDeliveryStatus(String trackingNumber, DeliveryStatus deliveryStatus) {
                Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber);

                delivery.setDeliveryStatus(deliveryStatus);
                String message = "주문번호: " + delivery.getOrderId() + "의 배송상태가 "
                                + deliveryStatus + "로 변경되었습니다. 배송조회에서 확인해주세요!";
                sseEmitterManager.broadcast(message);

                // 배송 상태가 배송완료일 시 order의 status를 배송중-> 배송완료로 변경
                if ("배송완료".equals(deliveryStatus)) {
                        orderService.updateOrderStatus(delivery.getOrderId(), OrderStatus.DELIVERED);
                } else {
                        // 배송 완료 외에는 배송중으로 변경
                        orderService.updateOrderStatus(delivery.getOrderId(), OrderStatus.SHIPPING);
                }

                return deliveryRepository.save(delivery);
        }

        public Page<ShippingListDto> getAllDeliveries(Pageable pageable) {
                return orderRepository.findShippingList(pageable);
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

}
