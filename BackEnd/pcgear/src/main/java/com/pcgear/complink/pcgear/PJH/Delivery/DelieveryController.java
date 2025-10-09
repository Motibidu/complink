package com.pcgear.complink.pcgear.PJH.Delivery;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingResponse;
import com.pcgear.complink.pcgear.PJH.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;
import com.pcgear.complink.pcgear.PJH.Delivery.model.TrackingNumberReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
        String accessToken = deliveryService.getAccessToken(trackingNumberReq);
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
    public ResponseEntity<String> handleWebhookRequest(@RequestBody TrackingNumberReq trackingNumberReq) {
        String accessToken = deliveryService.getAccessToken(trackingNumberReq);

        TrackingResponse trackingResponse = deliveryService.trackDelivery(trackingNumberReq.getCarrierId(),
                trackingNumberReq.getTrackingNumber(), accessToken).block();
        if (trackingResponse.getData().getTrack().getLastEvent().getStatus().getName().equals("배송완료")) {
            // carrierId, trackingNumber로 order 찾기
            // orderId로 order 찾고 OrderStatus 업데이트 하기
            orderService.updateOrderStatus(null, null);
        }
        log.info("trackingResponse: {}", trackingResponse);

        return ResponseEntity.ok("웹훅 수신완료!");
    }

}
