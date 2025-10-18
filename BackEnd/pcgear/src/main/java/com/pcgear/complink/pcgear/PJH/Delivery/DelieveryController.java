package com.pcgear.complink.pcgear.PJH.Delivery;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.PJH.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.PJH.Delivery.model.WebhookReq;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderStatus;
import java.util.Optional;
import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;

import jakarta.persistence.EntityNotFoundException;

import com.pcgear.complink.pcgear.PJH.Delivery.model.Delivery;
import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
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
    private final OrderService orderService;

    @PostMapping("/trackingNumber")
    public Mono<ResponseEntity<String>> registerWebhook(@RequestBody TrackingNumberReq trackingNumberReq) {
        log.info("waybillReq: {}", trackingNumberReq);

        // 토큰 생성
        String accessToken = deliveryService.getAccessToken();
        log.info("accessToken: {}", accessToken);

        return deliveryService
                .registerWebhookIfValid(accessToken, trackingNumberReq,
                        DELIVERYTRACKER_WEBHOOK_URL + "/delivery/webhook")
                .map(result -> {
                    if (result.isValid()) {
                        return ResponseEntity.ok(result.getMessage());
                    } else {
                        // 클라이언트의 요청이 잘못되었을 가능성이 높으므로 400 Bad Request가 더 적합합니다.
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
                    }
                });
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhookRequest(@RequestBody WebhookReq webhookReq) {
        // 토큰 발급
        String accessToken = deliveryService.getAccessToken();
        log.info("Webhook received!! accessToken: {}", accessToken);
        log.info("webhookReq: {}", webhookReq);

        // 배송 조회
        TrackingResponse trackingResponse = deliveryService.trackDelivery(webhookReq.getCarrierId(),
                webhookReq.getTrackingNumber(), accessToken).block();
        log.info("trackingResponse: {}", trackingResponse);

        // Delivery, Order의 status 업데이트
        String currentStatus = deliveryService.getDeliveryStatus(trackingResponse);
        log.info("currentStatus: {}", currentStatus);
        deliveryService.updateDeiliveryStatus(webhookReq, currentStatus);

        return ResponseEntity.ok("웹훅 수신완료!");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Delivery> readDelivery(@PathVariable(name = "orderId") Integer orderId) {
        Optional<Delivery> deliveryOpt = deliveryService.findByOrderId(orderId);
        return deliveryOpt
                .map(ResponseEntity::ok) // delivery가 있다면 ResponseEntity에 감싸서 반환
                .orElseGet(() -> ResponseEntity.noContent().build()); // 없으면 204 반환

    }

}
