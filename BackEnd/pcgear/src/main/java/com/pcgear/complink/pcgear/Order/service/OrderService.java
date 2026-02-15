package com.pcgear.complink.pcgear.Order.service;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.DeliveryService;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;
import com.pcgear.complink.pcgear.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.Item.ItemRepository;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Order.event.OrderCreatedEvent;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailReqDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailRespDto;
import com.pcgear.complink.pcgear.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderSearchCondition;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderItem;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Payment.Payment;
import com.pcgear.complink.pcgear.Payment.PaymentLinkService;
import com.pcgear.complink.pcgear.Payment.PaymentRepository;
import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;
import com.pcgear.complink.pcgear.Sell.SellRepository;
import com.pcgear.complink.pcgear.Sell.SellService;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;
import com.pcgear.complink.pcgear.exception.PaymentProcessingException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;

    @Lazy
    private final PaymentLinkService paymentLinkService;
    private final SellService sellService;
    @Lazy
    private final DeliveryService deliveryService;
    private final ItemService itemService;
    @Lazy
    private final OrderService self;

    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(value = { "dashboard-summary" }, allEntries = true)
    public Order createOrder(OrderRequestDto requestDto) {
        log.info("requestDto: {}", requestDto);

        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("거래처 정보를 찾을 수 없습니다. ID: " + requestDto.getCustomerId()));
        String uuid = UUID.randomUUID().toString();
        String merchantUid = "PCG-" + uuid;
        String paymentLink;

        try {
            paymentLink = paymentLinkService.createPaymentLink(
                    merchantUid,
                    requestDto.getGrandAmount().intValue(),
                    customer.getCustomerName() + "님의 주문",
                    customer.getPhoneNumber()); // 👈 여기서 3초가 걸려도 DB에는 아무 영향이 없습니다.
        } catch (RuntimeException e) {
            throw new RuntimeException("주문 생성 중 결제 링크 생성 실패: " + e.getMessage(), e);
        }

        // 4. Repository를 통해 DB에 저장
        try {
            return self.processOrderCreation(requestDto, merchantUid, paymentLink);
        } catch (Exception e) {
            paymentLinkService.cancelPaymentLink(paymentLink);
            log.error("주문 생성 트랜잭션 롤백 및 결제 링크 취소: {}", e.getMessage());
            throw new RuntimeException("주문 생성 중 오류 발생 및 결제 링크 취소 완료", e);

        }
    }

    @Transactional
    public Order processOrderCreation(OrderRequestDto requestDto, String merchantUid, String paymentLink) {
        log.info("processOrderCreation 시작 - DB 저장 트랜잭션 시작");

        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("거래처 정보를 찾을 수 없습니다."));

        UserEntity manager = userRepository.findByUsername(requestDto.getManagerId())
                .orElseThrow(() -> new EntityNotFoundException("담당자 정보를 찾을 수 없습니다."));

        // 엔티티 생성
        Order order = new Order();
        order.setOrderDate(requestDto.getOrderDate());
        order.setDeliveryDate(requestDto.getDeliveryDate());
        order.setOrderStatus(OrderStatus.ORDER_RECEIVED);
        order.setCustomer(customer);
        order.setManager(manager);
        order.setTotalAmount(requestDto.getTotalAmount());
        order.setVatAmount(requestDto.getVatAmount());
        order.setGrandAmount(requestDto.getGrandAmount());
        order.setMerchantUid(merchantUid);
        order.setPaymentLink(paymentLink);

        // 주문 상품 추가
        for (OrderRequestDto.OrderItemDto itemDto : requestDto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemCategory(itemDto.getItemCategory());
            orderItem.setSerialNumRequired(itemDto.getItemCategory().isSerialNumRequired());
            orderItem.setItem(itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("품목 찾기 실패")));
            orderItem.setItemName(itemDto.getItemName());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(itemDto.getUnitPrice());
            orderItem.setTotalPrice(itemDto.getTotalPrice());

            order.addItem(orderItem);

        }

        // 가용재고 차감
        itemService.updateItemAvailableQuantity(order);

        Order savedOrder = orderRepository.save(order); // 저장 후 즉시 커밋

        // 트랜잭션 커밋 후 알림 전송
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getOrderId(), "주문서가 성공적으로 생성되었습니다."));

        return savedOrder;
    }

    public List<OrderResponseDto> findByOrderStatus(OrderStatus orderStatus) {
        return orderRepository.findByOrderStatus(orderStatus).stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "dashboard-summary", allEntries = true)
    public Order updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderIdWithFetchJoin(orderId)
                .orElseThrow(
                        () -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order setPaidAt(Order order) {
        order.setPaidAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Page<AssemblyQueueRespDto> getAllAssemblyQueue(List<OrderStatus> statusesToFind, Pageable pageable) {
        return orderRepository.findAllByOrderStatusIn(statusesToFind, pageable);
    }

    @Transactional(readOnly = true)
    public AssemblyDetailRespDto getAssemblyDetailRespDto(Integer orderId) {
        Order order = orderRepository.findByIdWithItemsAndCustomer(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        log.debug("order.getOrderItems(): {}", order.getOrderItems());

        AssemblyDetailRespDto respDto = AssemblyDetailRespDto.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus())
                .customer(order.getCustomer())
                .assemblyStatus(order.getAssemblyStatus())
                .orderItems(order.getOrderItems())
                .build();
        return respDto;
    }

    public Order setSerialNumber(Integer orderId, List<OrderItem> orderItemss) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));
        List<OrderItem> orderItems = order.getOrderItems();
        orderItems.forEach(orderItem -> {
            orderItemss.forEach(orderItemWithSerial -> {
                if (orderItem.getOrderItemId().equals(orderItemWithSerial.getOrderItemId())) {
                    orderItem.setSerialNum(orderItemWithSerial.getSerialNum());
                }
            });
        });
        return orderRepository.save(order);

    }

    @Transactional
    public AssemblyDetailRespDto processAssemblyStatus(Integer orderId, AssemblyDetailReqDto assemblyDetailReqDto) {
        updateAssemblyStatus(orderId, assemblyDetailReqDto.getNextAssemblyStatus());

        // AssemblyStatus가 부품검수일 경우 OrderStatus 상품준비중으로 업데이트
        if (assemblyDetailReqDto.getNextAssemblyStatus() == AssemblyStatus.INSPECTING) {
            updateOrderStatus(orderId, OrderStatus.PREPARING_PRODUCT);
        }
        // AssemblyStatus가 완료일 경우(운송장번호 입력한 경우) OrderStatus 배송대기로 업데이트
        if (assemblyDetailReqDto.getNextAssemblyStatus() == AssemblyStatus.COMPLETED) {
            updateOrderStatus(orderId, OrderStatus.SHIPPING_PENDING);

            String accessToken = deliveryService.getAccessToken();
            log.info("accessToken: {}", accessToken);

            TrackingNumberReq trackingNumberReq = TrackingNumberReq.builder()
                    .orderId(orderId)
                    .customerId(assemblyDetailReqDto.getCustomerId())
                    .trackingNumber(assemblyDetailReqDto.getTrackingNumber())
                    .carrierId(assemblyDetailReqDto.getCarrierId())
                    .build();

            ValidationResult result = deliveryService
                    .registerWebhookIfValid(accessToken, trackingNumberReq);

            if (!result.isValid()) {
                // 웹훅 등록 실패 시 예외 처리 또는 로그
                log.error("Failed to register webhook: {}", result.getMessage());
                // 비즈니스 예외를 던져 트랜잭션 롤백 및 에러 응답 유도
                throw new RuntimeException("운송장 유효성 검사 또는 웹훅 등록 실패: " + result.getMessage());
            }
        }
        setSerialNumber(orderId, assemblyDetailReqDto.getOrderItems());
        return getAssemblyDetailRespDto(orderId);
    }

    public OrderResponseDto findOrderById(Integer orderId) {
        return orderRepository.findById(orderId).map(OrderResponseDto::new)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

    }

    private Order updateAssemblyStatus(Integer orderId, AssemblyStatus nextAssemblyStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));
        order.setAssemblyStatus(nextAssemblyStatus);

        return orderRepository.save(order);
    }

    public Order processOrderCancellation(Integer orderId, String reason) {
        // 0. 주문 존재 여부 확인
        orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        // 1. DB에서 먼저 취소 처리 (트랜잭션으로 보호)
        Order order = self.cancelOrderInDB(orderId);

        // 2. 결제 기록 확인 및 환불 처리
        Optional<Payment> paymentOpt = paymentRepository.findByOrder_OrderId(orderId);
        if (paymentOpt.isPresent()) {
            try {
                paymentLinkService.cancelPayment(orderId, reason);
                log.info("환불 처리 완료. OrderId: {}", orderId);
            } catch (Exception e) {
                // 3. 환불 실패 시 → 보상 트랜잭션 (DB 복구)
                log.error("환불 실패, DB 복구 시작. OrderId: {}, Error: {}", orderId, e.getMessage());
                self.compensateOrderCancellation(orderId);
                throw new PaymentProcessingException("결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        return order;
    }

    @Transactional
    public Order cancelOrderInDB(Integer orderId) {
        log.info("주문취소 시작, 주문Id: {}", orderId);

        // [멱등성 보장] 이미 취소된 주문인지 확인
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));
        if (existingOrder.getOrderStatus() == OrderStatus.CANCELLED) {
            log.info("이미 취소 처리된 주문입니다. 중복 로직을 건너뜁니다. OrderId: {}", orderId);
            return existingOrder;
        }

        // 항상 실행
        Order order = updateOrderStatus(orderId, OrderStatus.CANCELLED);
        itemService.restoreItemAvailableQuantity(orderId);

        // 결제기록 있을때만 실행
        paymentRepository.findByOrder_OrderId(orderId).ifPresent(payment -> {
            log.info("Payment 존재");

            // 결제 상태 결제취소로 업데이트
            payment.setPaymentStatus(PaymentStatus.CANCELLED);

            // 판매기록에 - 매출 데이터 추가
            sellService.createNegateSell(orderId);

            itemService.restoreItemQuantityOnHand(orderId);
        });

        return order;
    }

    /**
     * 보상 트랜잭션: 환불 실패 시 DB를 원래 상태로 복구
     */
    @Transactional
    public void compensateOrderCancellation(Integer orderId) {
        log.info("보상 트랜잭션 시작 - 주문 취소를 되돌립니다. OrderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        // 주문 상태를 결제완료로 복구
        order.setOrderStatus(OrderStatus.PAID);

        // 가용재고 다시 차감
        itemService.updateItemAvailableQuantity(order);

        paymentRepository.findByOrder_OrderId(orderId).ifPresent(payment -> {
            // 결제 상태 복구
            payment.setPaymentStatus(PaymentStatus.PAID);

            // 판매기록 복구 (네거티브 매출 제거)
            sellService.removeNegateSell(orderId);

            // 실재고 다시 차감
            itemService.updateItemQuantityOnHand(order);
        });

        orderRepository.save(order);
        log.info("보상 트랜잭션 완료. OrderId: {}", orderId);
    }

    public Page<OrderResponseDto> searchOrders(OrderSearchCondition condition, Pageable pageable) {
        return orderRepository.searchOrders(condition, pageable);
    }

}