package com.pcgear.complink.pcgear.PJH.Delivery;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.PJH.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.PJH.Delivery.model.WebhookReq;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;
import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
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

    private final DeliveryService deliveryService;
    private final OrderService orderService;

    @PostMapping("/trackingNumber")
    public ResponseEntity<String> registerWebhook(@RequestBody TrackingNumberReq trackingNumberReq) {
        log.info("waybillReq: {}", trackingNumberReq);

        // 토큰 생성
        String accessToken = deliveryService.getAccessToken();
        log.info("accessToken: {}", accessToken);

        Mono<ValidationResult> webhookRegistered = deliveryService.registerWebhookIfValid(accessToken,
                trackingNumberReq, "https://76eb83593b8c.ngrok-free.app/delivery/webhook");

        log.info("webhookRegistered: {}", webhookRegistered.block());

        if (webhookRegistered.block().isValid()) {

            return ResponseEntity.ok("추적 등록에 완료했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(webhookRegistered.block().getMessage());
        }

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

        // Delivery의 status 업데이트
        String currentStatus = deliveryService.getDeliveryStatus(trackingResponse);
        log.info("currentStatus: {}", currentStatus);
        deliveryService.updateDeiliveryStatus(webhookReq, currentStatus);

        return ResponseEntity.ok("웹훅 수신완료!");
    }

    @GetMapping("/registered/{orderId}")
    public ResponseEntity getMethodName(@PathVariable(name = "orderId") Integer orderId) {
        boolean exists = deliveryService.existsByOrderId(orderId);

        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
