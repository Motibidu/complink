package com.pcgear.complink.pcgear.Order.service;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.DeliveryService;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;
import com.pcgear.complink.pcgear.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.Item.ItemRepository;
import com.pcgear.complink.pcgear.Manager.Manager;
import com.pcgear.complink.pcgear.Manager.ManagerRepository;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailReqDto;
import com.pcgear.complink.pcgear.Order.model.AssemblyDetailRespDto;
import com.pcgear.complink.pcgear.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.model.AssemblyQueueRespDto;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderItem;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Payment.PaymentLinkService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// @RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ManagerRepository managerRepository;
    private final CustomerRepository customerRepository;
    private final PaymentLinkService paymentLinkService;
    private final ItemRepository itemRepository;
    private final DeliveryService deliveryService;

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${delivery-tracker.webhook-url}")
    private String DELIVERYTRACKER_WEBHOOK_URL;

    public OrderService(OrderRepository orderRepository,
            ManagerRepository managerRepository,
            CustomerRepository customerRepository,
            PaymentLinkService paymentLinkService,
            ItemRepository itemRepository,
            @Lazy DeliveryService deliveryService, // 👈 4. 순환 참조 대상에 @Lazy 추가
            SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.managerRepository = managerRepository;
        this.customerRepository = customerRepository;
        this.paymentLinkService = paymentLinkService;
        this.itemRepository = itemRepository;
        this.deliveryService = deliveryService;
        this.messagingTemplate = messagingTemplate;
    }

    @CacheEvict(value = { "dashboard-summary" }, allEntries = true)
    @Transactional
    public Order createOrder(OrderRequestDto requestDto) {
        log.info("requestDto: {}", requestDto);
        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("거래처 정보를 찾을 수 없습니다. ID: " + requestDto.getCustomerId()));

        Manager manager = null;
        if (requestDto.getManagerName() != null) {
            manager = managerRepository.findById(requestDto.getManagerId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("담당자 정보를 찾을 수 없습니다. ID: " + requestDto.getManagerId()));
        }

        // 2. DTO -> Entity 변환
        Order order = new Order();
        order.setOrderDate(requestDto.getOrderDate());
        order.setDeliveryDate(requestDto.getDeliveryDate());
        order.setOrderStatus(OrderStatus.ORDER_RECEIVED);

        order.setCustomer(customer);
        order.setManager(manager);

        order.setTotalAmount(requestDto.getTotalAmount());
        order.setVatAmount(requestDto.getVatAmount());
        order.setGrandAmount(requestDto.getGrandAmount());

        try {
            String uuid = UUID.randomUUID().toString();
            String merchantUid = "PCG-" + uuid;

            String paymentLink = paymentLinkService.createPaymentLink(
                    merchantUid,
                    requestDto.getGrandAmount().intValue(),
                    customer.getCustomerName() + "님의 주문",
                    customer.getPhoneNumber());
            order.setPaymentLink(paymentLink);
            order.setMerchantUid(merchantUid);
        } catch (RuntimeException e) {
            throw new RuntimeException("주문 생성 중 결제 링크 생성 실패: " + e.getMessage(), e);
        }

        // 3. 주문 아이템 리스트 변환 및 추가
        for (OrderRequestDto.OrderItemDto itemDto : requestDto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemCategory(itemDto.getItemCategory());
            orderItem.setSerialNumRequired(itemDto.getItemCategory().isSerialNumRequired());
            orderItem.setItem(itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("품목 정보를 찾을 수 없습니다. ID: " + itemDto.getItemId())));
            orderItem.setItemName(itemDto.getItemName());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(itemDto.getUnitPrice());
            orderItem.setTotalPrice(itemDto.getTotalPrice());

            order.addItem(orderItem);
        }

        String message = "주문서가 성공적으로 생성되었습니다.";
        messagingTemplate.convertAndSend("/topic/notifications", message);

        // 4. Repository를 통해 DB에 저장
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true) // 이 어노테이션이 반드시 있어야 합니다.
    public List<OrderResponseDto> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDto::new) // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "dashboard-summary", allEntries = true)
    public void deleteOrder(Integer orderId) {
        orderRepository.deleteById(orderId);
    }

    public List<OrderResponseDto> findByOrderStatus(OrderStatus orderStatus) {
        return orderRepository.findByOrderStatus(orderStatus).stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "dashboard-summary", allEntries = true)
    public Order updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order setPaidAt(Order order) {
        order.setPaidAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<AssemblyQueueRespDto> readAssemblyQueueOrders(List<OrderStatus> orderStatus) {
        return orderRepository.findAllByOrderStatusIn(orderStatus).stream()
                .map(AssemblyQueueRespDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssemblyDetailRespDto getAssemblyDetailRespDto(Integer orderId) {
        Order order = orderRepository.findByIdWithItemsAndCustomer(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        System.out.println("order.getOrderItems(): " + order.getOrderItems()); // 이제 데이터가 출력될 것입니다.

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

    @CacheEvict(value = "dashboard-summary", allEntries = true)
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
                    .registerWebhookIfValid(accessToken, trackingNumberReq,
                            DELIVERYTRACKER_WEBHOOK_URL + "/delivery/webhook")
                    .block();

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
}