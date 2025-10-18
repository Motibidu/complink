package com.pcgear.complink.pcgear.Order.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "주문서 API", description = "주문서를 관리하는 API")
@RestController
@Slf4j
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @DeleteMapping("{orderId}")
    public ResponseEntity deleteOrder(@PathVariable(name = "orderId") Integer orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<String> createNewOrder(@RequestBody OrderRequestDto orderRequestDto) {
        orderService.createOrder(orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("주문이 성공적으로 생성되었습니다.");
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> readOrders(
            @RequestParam(name = "orderStatus", required = false) String orderStatusParam) {
        if (orderStatusParam != null && !orderStatusParam.trim().isEmpty()) {
            try {
                OrderStatus status = OrderStatus.fromNameIgnoreCase(orderStatusParam);

                return ResponseEntity.ok(orderService.findByOrderStatus(status));
            } catch (IllegalArgumentException e) {
                log.warn("유효하지 않은 주문 상태 파라미터: {}", orderStatusParam);
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            return ResponseEntity.ok(orderService.findAllOrders());
        }
    }
}
