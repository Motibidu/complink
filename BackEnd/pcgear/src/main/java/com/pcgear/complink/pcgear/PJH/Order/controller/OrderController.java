package com.pcgear.complink.pcgear.PJH.Order.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.PJH.Order.model.Customer;
import com.pcgear.complink.pcgear.PJH.Order.model.Manager;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;
import com.pcgear.complink.pcgear.PJH.Register.model.Item;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "주문서 등록 API", description = "주문서를 관리하는 API")
@RestController
@RequestMapping("/order") // 이 컨트롤러의 모든 경로는 '/order'로 시작
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/new")
    public ResponseEntity<String> createNewOrder(@RequestBody OrderRequestDto orderRequestDto) {
        orderService.createOrder(orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("주문이 성공적으로 생성되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponseDto>> findOrders() {
        List<OrderResponseDto> orderDtos = orderService.findAllOrders();
        return ResponseEntity.ok(orderDtos);
    }

    @GetMapping("/findAllCustomers")
    public ResponseEntity<List<Customer>> findAllCustmers() {
        List<Customer> customers = orderService.findAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/findAllManagers")
    public ResponseEntity<List<Manager>> findAllManagers() {
        List<Manager> managers = orderService.findAllManagers();
        return ResponseEntity.ok(managers);
    }

    @GetMapping("/findAllItems")
    public ResponseEntity<List<Item>> findAllItems() {
        List<Item> items = orderService.findAllItems();
        return ResponseEntity.ok(items);
    }
}
