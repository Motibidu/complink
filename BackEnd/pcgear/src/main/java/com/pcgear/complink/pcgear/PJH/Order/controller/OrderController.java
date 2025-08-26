package com.pcgear.complink.pcgear.PJH.Order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;

@RestController
@RequestMapping("/order") // 이 컨트롤러의 모든 경로는 '/order'로 시작
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/new")
    public ResponseEntity<String> createNewOrder(@RequestBody OrderRequestDto orderRequestDto) {
        // System.out.println(orderRequestDto);
        try {
            orderService.createOrder(orderRequestDto);
            return ResponseEntity.ok("주문이 성공적으로 생성되었습니다.");
        } catch (Exception e) {
            // 간단한 예외 처리
            return ResponseEntity.badRequest().body("주문 생성 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponseDto>> findOrders() {
        List<OrderResponseDto> orderDtos = orderService.findAllOrders();
        return ResponseEntity.ok(orderDtos);
    }
}
