package com.pcgear.complink.pcgear.PJH.Order.controller;

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

import com.pcgear.complink.pcgear.PJH.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "주문서 API", description = "주문서를 관리하는 API")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @DeleteMapping("{orderId}")
    public ResponseEntity deleteOrder(@PathVariable(name = "orderId") Long orderId) {
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
            @RequestParam(name = "status", required = false) String status) {
        if (status != null) {
            // status 파라미터가 있다면 해당 상태의 주문만 조회
            return ResponseEntity.ok(orderService.findOrdersByStatus(status));
        } else {
            // status 파라미터가 없다면 전체 주문 조회
            return ResponseEntity.ok(orderService.findAllOrders());
        }
    }
}
