package com.pcgear.complink.pcgear.Order.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailReqDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailRespDto;
import com.pcgear.complink.pcgear.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderSearchCondition;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.Payment.PaymentLinkService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import retrofit2.http.Path;

@Tag(name = "주문서 API", description = "주문서를 관리하는 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
@RestController
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> searchOrders(
            @ModelAttribute(name = "params") OrderSearchCondition condition, // 쿼리 파라미터를
            // DTO로 자동 바인딩
            @PageableDefault(size = 20, sort = "orderId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("condition: {}", condition);

        return ResponseEntity.ok(orderService.searchOrders(condition, pageable));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable(name = "orderId") Integer orderId) {

        Order canceledOrder = orderService.processOrderCancellation(orderId, "단순 변심에 의한 취소");

        return ResponseEntity.ok(new OrderResponseDto(canceledOrder));
    }

    @PostMapping("/admin/{orderId}/force-cancel")
    @PreAuthorize("hasRole('ADMIN')") // 최고 관리자만 가능
    public ResponseEntity<OrderResponseDto> forceCancelOrder(@PathVariable(name = "orderId") Integer orderId) {

        // 외부 API 호출 없이 DB만 정리하는 서비스 호출
        Order canceledOrder = orderService.cancelOrderInDB(orderId);

        return ResponseEntity.ok(new OrderResponseDto(canceledOrder));
    }

    @PostMapping
    public ResponseEntity<String> createNewOrder(@RequestBody OrderRequestDto orderRequestDto) {
        Order newOrder = orderService.createOrder(orderRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("주문이 성공적으로 생성되었습니다.");
    }

    @GetMapping("/{orderId}/assembly-detail")
    public ResponseEntity<AssemblyDetailRespDto> readAssemblyDetail(@PathVariable(name = "orderId") Integer orderId) {
        AssemblyDetailRespDto assemblyDetailRespDto = orderService.getAssemblyDetailRespDto(orderId);
        return ResponseEntity.ok(assemblyDetailRespDto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> readOrder(@PathVariable(name = "orderId") Integer orderId) {
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    @GetMapping("/assembly-queue")
    public ResponseEntity<Page<AssemblyQueueRespDto>> readAssemblyQueueOrders(
            @RequestParam(name = "orderStatus", required = false) List<OrderStatus> orderStatus,
            @PageableDefault(size = 15, sort = "orderId", direction = Sort.Direction.DESC) Pageable pageable) {

        List<OrderStatus> statusesToFind = (orderStatus == null || orderStatus.isEmpty())
                ? List.of(OrderStatus.PAID, OrderStatus.PREPARING_PRODUCT, OrderStatus.SHIPPING_PENDING,
                        OrderStatus.SHIPPING)
                : orderStatus;

        Page<AssemblyQueueRespDto> assemblyQueuePage = orderService.getAllAssemblyQueue(statusesToFind, pageable);

        return ResponseEntity.ok(assemblyQueuePage);
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
