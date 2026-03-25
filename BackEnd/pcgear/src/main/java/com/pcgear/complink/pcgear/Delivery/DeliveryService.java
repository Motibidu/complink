package com.pcgear.complink.pcgear.Delivery;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.event.DeliveryStatusUpdatedEvent;
import com.pcgear.complink.pcgear.Delivery.event.DeliveryWebhookRegisterEvent;
import com.pcgear.complink.pcgear.Delivery.exception.InvalidTrackingNumberException;
import com.pcgear.complink.pcgear.Delivery.enums.DeliveryStatus;
import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Delivery.model.req.GraphQLReq;
import com.pcgear.complink.pcgear.Delivery.model.req.TrackingNumberReq;
import com.pcgear.complink.pcgear.Delivery.model.resp.AccessTokenResp;
import com.pcgear.complink.pcgear.Delivery.model.resp.TrackingResp;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.properties.DeliveryTrackerProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 배송 서비스 (Orchestrator)
 * - 배송 추적 등록 흐름 조정
 * - 운송장 검증
 * - 배송 조회
 * - AccessToken 발급
 *
 * 책임: 전체 흐름 조정만 담당 (상태 관리는 DeliveryStateManager에 위임)
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {

        private final DeliveryStateManager stateManager;
        private final ApplicationEventPublisher eventPublisher;
        private final DeliveryRepository deliveryRepository;
        private final OrderRepository orderRepository;
        private final OrderService orderService;
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
         * 배송 추적 등록 (Orchestrator)
         * 1. 운송장 검증
         * 2. 배송 정보 저장 (PENDING)
         * 3. 웹훅 등록 이벤트 발행
         *
         * @throws InvalidTrackingNumberException 운송장 번호 검증 실패 (GlobalExceptionHandler 처리)
         * @throws WebhookRegistrationException   웹훅 등록 실패 (GlobalExceptionHandler 처리)
         */
        @Transactional
        public void registerDeliveryTracking(String accessToken, TrackingNumberReq request) {
                log.info("배송 추적 등록 시작: {}", request);

                // 1. 운송장 검증
                validateTrackingNumber(accessToken, request.getCarrierId(), request.getTrackingNumber());

                // 2. 배송 정보 저장 (상태 관리 위임)
                Delivery delivery = stateManager.createDeliveryAsPending(request);

                // 3. 웹훅 등록 이벤트 발행 (비동기)
                eventPublisher.publishEvent(new DeliveryWebhookRegisterEvent(
                        accessToken,
                        request.getCarrierId(),
                        request.getTrackingNumber(),
                        properties.getWebhookUrl(),
                        delivery.getDeliveryId(),
                        delivery.getOrderId()));

                log.info("배송 추적 등록 요청 완료: OrderId={}, DeliveryId={}",
                        request.getOrderId(), delivery.getDeliveryId());
        }

        // 배송 조회
        public TrackingResp trackDelivery(String carrierId, String trackingNumber, String accessToken) {
                Map<String, String> variables = Map.of(
                                "carrierId", carrierId,
                                "trackingNumber", trackingNumber);

                GraphQLReq requestBody = GraphQLReq.builder()
                                .query(TRACK_DELIVERY_QUERY)
                                .variables(variables)
                                .build();

                return restClient.post()
                                .uri(properties.getGraphqlApiUrl())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(requestBody)
                                .retrieve()
                                .body(TrackingResp.class);
        }

        /**
         * 운송장 번호 검증
         *
         * @throws InvalidTrackingNumberException 검증 실패 시
         */
        private void validateTrackingNumber(String accessToken, String carrierId, String trackingNumber) {
                TrackingResp response = trackDelivery(carrierId, trackingNumber, accessToken);
                log.info("배송 조회 결과: {}", response);

                if (response == null || response.getData() == null || response.getData().getTrack() == null) {
                        String errorMessage = extractErrorMessage(response);
                        throw new InvalidTrackingNumberException(errorMessage);
                }
        }

        private String extractErrorMessage(TrackingResp response) {
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


        public String extractDeliveryStatus(TrackingResp trackingResponse) {
                return Optional.ofNullable(trackingResponse)
                                .map(TrackingResp::getData)
                                .map(TrackingResp.TrackingData::getTrack)
                                .map(TrackingResp.Track::getLastEvent)
                                .map(TrackingResp.LastEvent::getStatus)
                                .map(TrackingResp.EventStatus::getName)
                                .orElse(null);
        }

        @Transactional
        public Delivery updateDeliveryStatus(String trackingNumber, DeliveryStatus deliveryStatus) {
                Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber);

                delivery.setDeliveryStatus(deliveryStatus);

                // 배송 상태가 배송완료일 시 order의 status를 배송중-> 배송완료로 변경
                if (deliveryStatus.equals(DeliveryStatus.DELIVERED)) {
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


        /**
         * PENDING/FAILED 상태인 배송 목록 조회 (재시도 대상)
         */
        // public List<Delivery> getFailedWebhookRegistrations() {
        //         return deliveryRepository.findPendingWebhookRegistrations();
        // }

}
