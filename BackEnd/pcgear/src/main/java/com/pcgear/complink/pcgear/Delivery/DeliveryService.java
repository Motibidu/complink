package com.pcgear.complink.pcgear.Delivery;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.event.DeliveryStatusUpdatedEvent;
import com.pcgear.complink.pcgear.Delivery.exception.DeliveryTrackingException;
import com.pcgear.complink.pcgear.Delivery.exception.InvalidTrackingNumberException;
import com.pcgear.complink.pcgear.Delivery.exception.WebhookRegistrationException;
import com.pcgear.complink.pcgear.Delivery.model.AccessTokenResp;
import com.pcgear.complink.pcgear.Delivery.model.DeliveryStatus;
import com.pcgear.complink.pcgear.Delivery.model.GraphQLRequest;
import com.pcgear.complink.pcgear.Delivery.model.RegisterWebhookResp;
import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Delivery.model.TrackingResponse;
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

        private final ApplicationEventPublisher eventPublisher;
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

        /**
         * 배송 추적 등록 (운송장 검증 + DB 저장 + 웹훅 등록)
         *
         * @throws InvalidTrackingNumberException 운송장 번호 검증 실패
         * @throws WebhookRegistrationException   웹훅 등록 실패
         * @throws DeliveryTrackingException      시스템 오류
         */
        public void registerDeliveryTracking(String accessToken, TrackingNumberReq request) {
                log.info("배송 추적 등록 시작: {}", request);

                try {
                        // 1. 운송장 검증 (DB 건드리지 않음)
                        validateTrackingNumber(accessToken, request.getCarrierId(), request.getTrackingNumber());

                        // 2. DB 저장 (트랜잭션)
                        Delivery savedDelivery = self.processRegistrationTransaction(request);

                        try {
                                // 3. 웹훅 등록 (외부 API)
                                registerWebhook(accessToken, request.getCarrierId(), request.getTrackingNumber(),
                                                properties.getWebhookUrl() + "/delivery/webhook");

                                log.info("배송 추적 등록 완료: OrderId={}", request.getOrderId());
                        } catch (WebhookRegistrationException e) {
                                // 웹훅 등록 실패 시 보상 로직: DB 롤백
                                log.error("웹훅 등록 실패, DB 롤백 시작: DeliveryId={}", savedDelivery.getDeliveryId());
                                compensateDeliveryRegistration(savedDelivery, request.getOrderId());
                                throw e;
                        }

                } catch (InvalidTrackingNumberException | WebhookRegistrationException e) {
                        // 비즈니스 예외는 그대로 전파
                        throw e;
                } catch (Exception e) {
                        log.error("배송 추적 등록 시스템 오류", e);
                        throw new DeliveryTrackingException("배송 추적 등록 중 시스템 오류가 발생했습니다", e);
                }
        }

        // 배송 조회
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

        /**
         * 운송장 번호 검증
         *
         * @throws InvalidTrackingNumberException 검증 실패 시
         */
        private void validateTrackingNumber(String accessToken, String carrierId, String trackingNumber) {
                TrackingResponse response = trackDelivery(carrierId, trackingNumber, accessToken);
                log.info("배송 조회 결과: {}", response);

                if (response == null || response.getData() == null || response.getData().getTrack() == null) {
                        String errorMessage = extractErrorMessage(response);
                        throw new InvalidTrackingNumberException(errorMessage);
                }
        }

        private String extractErrorMessage(TrackingResponse response) {
                if (response != null && response.getErrors() != null && !response.getErrors().isEmpty()) {
                        String errorMsg = response.getErrors().get(0).getMessage();
                        if ("Carrier not found".equals(errorMsg)) {
                                return "택배사 코드를 찾지 못했습니다.";
                        } else if (!errorMsg.isEmpty()) {
                                return "조회 오류: " + errorMsg;
                        }
                }
                return "운송장 번호를 찾지 못했습니다.";
        }

        /**
         * 배송 추적 웹훅 등록
         *
         * @throws WebhookRegistrationException 웹훅 등록 실패 시
         */
        private void registerWebhook(String accessToken, String carrierId, String trackingNumber,
                        String callbackUrl) {
                log.info("웹훅 등록 시작: carrierId={}, trackingNumber={}", carrierId, trackingNumber);

                RegisterWebhookInput input = RegisterWebhookInput.builder()
                                .carrierId(carrierId)
                                .trackingNumber(trackingNumber)
                                .callbackUrl(callbackUrl)
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

                log.info("웹훅 등록 응답: {}", response);

                if (response != null && response.getData() != null
                                && Boolean.TRUE.equals(response.getData().getRegisterTrackWebhook())) {
                        log.info("웹훅 등록 성공");
                        return;
                }

                // 실패 시 예외 던지기
                String errorMessage = "웹훅 등록 실패";
                if (response != null && response.getErrors() != null && !response.getErrors().isEmpty()) {
                        errorMessage = response.getErrors().get(0).getMessage();
                }
                log.error("웹훅 등록 실패: {}", errorMessage);
                throw new WebhookRegistrationException(errorMessage);
        }

        @Transactional
        public Delivery processRegistrationTransaction(TrackingNumberReq req) {
                // 배송 정보 생성
                Delivery delivery = createDelivery(req);

                // 주문 상태 변경 (배송 대기)
                orderService.updateOrderStatus(req.getOrderId(), OrderStatus.SHIPPING_PENDING);

                // 실재고 차감
                Order order = orderRepository.findById(req.getOrderId())
                                .orElseThrow(() -> new EntityNotFoundException("주문 없음: " + req.getOrderId()));
                itemService.updateItemQuantityOnHand(order);

                log.info("배송 등록 트랜잭션 완료");
                return delivery;
        }

        /**
         * 웹훅 등록 실패 시 보상 로직 (DB 롤백)
         */
        @Transactional
        public void compensateDeliveryRegistration(Delivery delivery, Integer orderId) {
                try {
                        // 1. 배송 정보 삭제
                        deliveryRepository.delete(delivery);

                        // 2. 주문 상태 복구 (배송 대기 -> 조립 완료)
                        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING_PENDING);

                        // 3. 실재고 복구
                        Order order = orderRepository.findById(orderId)
                                        .orElseThrow(() -> new EntityNotFoundException("주문 없음: " + orderId));
                        itemService.restoreItemQuantityOnHand(order);

                        log.info("DB 롤백 완료: DeliveryId={}, OrderId={}", delivery.getDeliveryId(), orderId);
                } catch (Exception e) {
                        log.error("DB 롤백 실패 - 수동 복구 필요: DeliveryId={}, OrderId={}", delivery.getDeliveryId(),
                                        orderId, e);
                        // 보상 로직 실패 시 예외는 던지지 않음 (원래 WebhookRegistrationException을 전파해야 하므로)
                }
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

                // 배송 상태가 배송완료일 시 order의 status를 배송중-> 배송완료로 변경
                if ("배송완료".equals(deliveryStatus)) {
                        orderService.updateOrderStatus(delivery.getOrderId(), OrderStatus.DELIVERED);
                } else {
                        // 배송 완료 외에는 배송중으로 변경
                        orderService.updateOrderStatus(delivery.getOrderId(), OrderStatus.SHIPPING);
                }

                Delivery savedDelivery = deliveryRepository.save(delivery);

                // 트랜잭션 커밋 후 알림 전송
                eventPublisher.publishEvent(new DeliveryStatusUpdatedEvent(
                                savedDelivery.getOrderId(),
                                deliveryStatus.toString()));

                return savedDelivery;
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
