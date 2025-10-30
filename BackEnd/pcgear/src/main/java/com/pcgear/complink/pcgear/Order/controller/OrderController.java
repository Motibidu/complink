package com.pcgear.complink.pcgear.Order.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailReqDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailRespDto;
import com.pcgear.complink.pcgear.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import retrofit2.http.Path;

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

    @GetMapping("/{orderId}/assembly-detail")
    public ResponseEntity<AssemblyDetailRespDto> readAssemblyDetail(@PathVariable(name = "orderId") Integer orderId) {
        AssemblyDetailRespDto assemblyDetailRespDto = orderService.getAssemblyDetailRespDto(orderId);
        return ResponseEntity.ok(assemblyDetailRespDto);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> readOrders(
            @RequestParam(name = "orderStatus", required = false) OrderStatus orderStatusParam) {
        if (orderStatusParam != null && !orderStatusParam.getDescription().trim().isEmpty()) {
            return ResponseEntity.ok(orderService.findByOrderStatus(orderStatusParam));
        }
        return ResponseEntity.ok(orderService.findAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> readOrder(@PathVariable(name = "orderId") Integer orderId) {
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    @GetMapping("/assembly-queue")
    public ResponseEntity<List<AssemblyQueueRespDto>> readAssemblyQueueOrders(
            @RequestParam(name = "orderStatus", required = false) List<OrderStatus> orderStatus) {

        List<OrderStatus> statusesToFind = (orderStatus == null || orderStatus.isEmpty())
                ? List.of(OrderStatus.PAID, OrderStatus.PREPARING_PRODUCT, OrderStatus.SHIPPING_PENDING,
                        OrderStatus.SHIPPING,
                        OrderStatus.DELIVERED)
                : orderStatus;

        List<AssemblyQueueRespDto> orders = orderService.readAssemblyQueueOrders(statusesToFind);

        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/assembly-status")
    public ResponseEntity<AssemblyDetailRespDto> updateAssemblyStatus(
            @PathVariable(name = "orderId") Integer orderId,
            @RequestBody AssemblyDetailReqDto assemblyDetailReqDto) {

        AssemblyDetailRespDto updatedAssemblyDetailRespDto = orderService.processAssemblyStatus(orderId,
                assemblyDetailReqDto);
        return ResponseEntity.ok(updatedAssemblyDetailRespDto);

    }

}
