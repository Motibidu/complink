package com.pcgear.complink.pcgear.Delivery;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.Delivery.model.WebhookReq;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import com.pcgear.complink.pcgear.Order.service.OrderService;

import jakarta.persistence.EntityNotFoundException;

import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.model.DeliveryStatus;
import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/delivery")
public class DelieveryController {

    @Value("${delivery-tracker.webhook-url}")
    private String DELIVERYTRACKER_WEBHOOK_URL;
    private final DeliveryService deliveryService;

    // @PostMapping("/trackingNumber")
    // public Mono<ResponseEntity<String>> registerWebhook(@RequestBody
    // TrackingNumberReq trackingNumberReq) {
    // log.info("waybillReq: {}", trackingNumberReq);

    // // 토큰 생성
    // String accessToken = deliveryService.getAccessToken();
    // log.info("accessToken: {}", accessToken);

    // return deliveryService
    // .registerWebhookIfValid(accessToken, trackingNumberReq,
    // DELIVERYTRACKER_WEBHOOK_URL + "/delivery/webhook")
    // .map(result -> {
    // if (result.isValid()) {
    // return ResponseEntity.ok(result.getMessage());
    // } else {
    // // 클라이언트의 요청이 잘못되었을 가능성이 높으므로 400 Bad Request가 더 적합합니다.
    // return
    // ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
    // }
    // });
    // }

    @PostMapping("/trackingNumber")
    public ResponseEntity<String> registerWebhook(@RequestBody TrackingNumberReq trackingNumberReq) {
        log.info("waybillReq: {}", trackingNumberReq);

        // 1. 토큰 생성 (동기)
        String accessToken = deliveryService.getAccessToken();
        log.info("accessToken: {}", accessToken);

        // 2. 검증 및 웹훅 등록 (동기)
        // Service 내부에서 [API 호출 -> 검증 -> DB 트랜잭션] 순차 실행
        ValidationResult result = deliveryService.registerWebhookIfValid(
                accessToken,
                trackingNumberReq,
                DELIVERYTRACKER_WEBHOOK_URL + "/delivery/webhook");

        if (result.isValid()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            // 클라이언트의 요청이 잘못되었을 가능성이 높으므로 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhookRequest(@RequestBody WebhookReq webhookReq) {
        log.info("Webhook received!! req: {}", webhookReq);

        try {
            // 1. 토큰 발급
            String accessToken = deliveryService.getAccessToken();

            // 2. 배송 조회 (API 호출)
            // [변경] .block() 제거! (Service가 TrackingResponse 객체를 바로 반환함)
            TrackingResponse trackingResponse = deliveryService.trackDelivery(
                    webhookReq.getCarrierId(),
                    webhookReq.getTrackingNumber(),
                    accessToken);

            log.info("trackingResponse: {}", trackingResponse);

            // 3. 상태 추출 및 DB 업데이트
            String currentStatus = deliveryService.extractDeliveryStatus(trackingResponse);
            log.info("currentStatus: {}", currentStatus);

            if (currentStatus != null) {
                deliveryService.updateDeiliveryStatus(
                        webhookReq.getTrackingNumber(),
                        DeliveryStatus.fromDescription(currentStatus));
            } else {
                log.warn("배송 상태를 추출할 수 없습니다.");
            }

            return ResponseEntity.ok("웹훅 수신완료!");

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("웹훅 처리 실패");
        }
    }

    @GetMapping("/shipping-list")
    public ResponseEntity<Page<ShippingListDto>> getAllShippingList(
            @PageableDefault(size = 15, sort = "orderId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ShippingListDto> deliveryPage = deliveryService.getAllDeliveries(pageable);

        return ResponseEntity.ok(deliveryPage);
    }

}
