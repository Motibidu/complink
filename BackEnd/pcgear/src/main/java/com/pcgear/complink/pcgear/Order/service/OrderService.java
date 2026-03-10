package com.pcgear.complink.pcgear.Order.service;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.DeliveryService;
import com.pcgear.complink.pcgear.Delivery.exception.DeliveryTrackingException;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;
import com.pcgear.complink.pcgear.Item.Item;
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

import io.micrometer.core.instrument.Counter;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
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
@Service
public class OrderService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;

    private final PaymentLinkService paymentLinkService;
    private final SellService sellService;
    private final DeliveryService deliveryService;
    private final ItemService itemService;
    private final OrderService self;

    private final Counter orderCreatedCounter;
    private final Counter orderFailedCounter;

    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            ItemRepository itemRepository,
            @Lazy PaymentLinkService paymentLinkService,
            SellService sellService,
            @Lazy DeliveryService deliveryService,
            ItemService itemService,
            @Lazy OrderService self,
            ApplicationEventPublisher eventPublisher,
            @Qualifier("orderCreatedCounter") Counter orderCreatedCounter,
            @Qualifier("orderFailedCounter") Counter orderFailedCounter) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.paymentLinkService = paymentLinkService;
        this.sellService = sellService;
        this.deliveryService = deliveryService;
        this.itemService = itemService;
        this.self = self;
        this.eventPublisher = eventPublisher;
        this.orderCreatedCounter = orderCreatedCounter;
        this.orderFailedCounter = orderFailedCounter;
    }

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
                    customer.getPhoneNumber());
        } catch (RuntimeException e) {
            throw new RuntimeException("주문 생성 중 결제 링크 생성 실패: " + e.getMessage(), e);
        }

        // 4. Repository를 통해 DB에 저장
        try {
            orderCreatedCounter.increment();
            return self.processOrderCreation(requestDto, merchantUid, paymentLink);
        } catch (Exception e) {
            orderFailedCounter.increment();
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
        Order order = Order.builder()
                .orderDate(requestDto.getOrderDate())
                .deliveryDate(requestDto.getDeliveryDate())
                .orderStatus(OrderStatus.ORDER_RECEIVED)
                .assemblyStatus(AssemblyStatus.QUEUE)
                .customer(customer)
                .manager(manager)
                .totalAmount(requestDto.getTotalAmount())
                .vatAmount(requestDto.getVatAmount())
                .grandAmount(requestDto.getGrandAmount())
                .merchantUid(merchantUid)
                .paymentLink(paymentLink)
                .build();

        // 주문 상품 추가
        for (OrderRequestDto.OrderItemDto itemDto : requestDto.getItems()) {
            // Item 조회
            Item item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("품목 찾기 실패: ID " + itemDto.getItemId()));

            // 백엔드에서 가격 계산 (보안: 프론트엔드 값 무시)
            BigDecimal unitPrice = item.getSellingPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            // Builder 패턴으로 OrderItem 생성
            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .serialNum(itemDto.getSerialNum())
                    .serialNumRequired(item.getItemCategory().isSerialNumRequired())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)        // Item에서 자동 복사 (반정규화)
                    .totalPrice(totalPrice)      // 백엔드에서 계산
                    .build();

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
        return orderRepository.findAssemblyQueue(statusesToFind, pageable);
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

    // 조립 상태 업데이트
    public AssemblyDetailRespDto processAssemblyStatus(Integer orderId, AssemblyDetailReqDto assemblyDetailReqDto) {
        // 1. DB 업데이트 (트랜잭션 보호)
        self.updateAssemblyInDB(orderId, assemblyDetailReqDto);

        // 2. 완료 상태일 경우 외부 API 호출 (트랜잭션 밖)
        if (assemblyDetailReqDto.getNextAssemblyStatus() == AssemblyStatus.COMPLETED) {
            try {
                String accessToken = deliveryService.getAccessToken();

                TrackingNumberReq trackingNumberReq = TrackingNumberReq.builder()
                        .orderId(orderId)
                        .customerId(assemblyDetailReqDto.getCustomerId())
                        .trackingNumber(assemblyDetailReqDto.getTrackingNumber())
                        .carrierId(assemblyDetailReqDto.getCarrierId())
                        .build();

                deliveryService.registerDeliveryTracking(accessToken, trackingNumberReq);
                log.info("배송 추적 등록 완료. OrderId: {}", orderId);

            } catch (DeliveryTrackingException e) {
                // 배송 추적 등록 실패 시 예외 발생 (상태는 PENDING으로 남아 수동 재시도 가능)
                log.error("배송 추적 등록 실패. OrderId: {}, Error: {}", orderId, e.getMessage());
                throw new RuntimeException("배송 추적 등록 실패: " + e.getMessage(), e);
            }
        }

        return getAssemblyDetailRespDto(orderId);
    }

    @Transactional
    public void updateAssemblyInDB(Integer orderId, AssemblyDetailReqDto assemblyDetailReqDto) {
        updateAssemblyStatus(orderId, assemblyDetailReqDto.getNextAssemblyStatus());

        // AssemblyStatus가 부품검수일 경우 OrderStatus 상품준비중으로 업데이트
        if (assemblyDetailReqDto.getNextAssemblyStatus() == AssemblyStatus.INSPECTING) {
            updateOrderStatus(orderId, OrderStatus.PREPARING_PRODUCT);
        }

        // AssemblyStatus가 완료일 경우 OrderStatus 배송대기로 업데이트
        if (assemblyDetailReqDto.getNextAssemblyStatus() == AssemblyStatus.COMPLETED) {
            updateOrderStatus(orderId, OrderStatus.SHIPPING_PENDING);
        }

        setSerialNumber(orderId, assemblyDetailReqDto.getOrderItems());
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
            paymentLinkService.cancelPayment(orderId, reason);
            log.info("환불 처리 완료. OrderId: {}", orderId);
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

    public Page<OrderResponseDto> searchOrders(OrderSearchCondition condition, Pageable pageable) {
        return orderRepository.searchOrders(condition, pageable);
    }

}