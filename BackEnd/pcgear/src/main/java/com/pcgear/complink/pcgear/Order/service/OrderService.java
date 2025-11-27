package com.pcgear.complink.pcgear.Order.service;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Delivery.DeliveryService;
import com.pcgear.complink.pcgear.Delivery.model.TrackingNumberReq;
import com.pcgear.complink.pcgear.Delivery.model.ValidationResult;
import com.pcgear.complink.pcgear.Item.ItemRepository;
import com.pcgear.complink.pcgear.Item.ItemService;
import com.pcgear.complink.pcgear.Manager.Manager;
import com.pcgear.complink.pcgear.Manager.ManagerRepository;
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
import com.pcgear.complink.pcgear.Payment.OrderPayment;
import com.pcgear.complink.pcgear.Payment.PaymentLinkService;
import com.pcgear.complink.pcgear.Payment.PaymentRepository;
import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;
import com.pcgear.complink.pcgear.Sell.Sell;
import com.pcgear.complink.pcgear.Sell.SellRepository;
import com.pcgear.complink.pcgear.Sell.SellService;
import com.pcgear.complink.pcgear.User.entity.UserEntity;
import com.pcgear.complink.pcgear.User.repository.UserRepository;
import com.pcgear.complink.pcgear.User.service.MailService;
import com.pcgear.complink.pcgear.properties.PortoneProperties;

import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// @RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PaymentLinkService paymentLinkService;
    private final SellService sellService;
    private final ItemRepository itemRepository;
    private final DeliveryService deliveryService;
    private final ItemService itemService;
    private final SellRepository sellRepository;
    private final PortoneProperties portoneProperties;
    private final OrderService self;
    private final JavaMailSender javaMailSender;

    private final SimpMessagingTemplate messagingTemplate;

    private final MailService mailService;

    @Value("${delivery-tracker.webhook-url}")
    private String DELIVERYTRACKER_WEBHOOK_URL;

    public OrderService(OrderRepository orderRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PaymentLinkService paymentLinkService,
            ItemRepository itemRepository,
            @Lazy DeliveryService deliveryService, // 👈 4. 순환 참조 대상에 @Lazy 추가
            SimpMessagingTemplate messagingTemplate,
            ItemService itemService,
            PortoneProperties portoneProperties, PaymentRepository paymentRepository, SellRepository sellRepository,
            SellService sellService,
            @Lazy OrderService self,
            MailService mailService,
            JavaMailSender javaMailSender) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.paymentLinkService = paymentLinkService;
        this.itemRepository = itemRepository;
        this.deliveryService = deliveryService;
        this.messagingTemplate = messagingTemplate;
        this.itemService = itemService;
        this.portoneProperties = portoneProperties;
        this.paymentRepository = paymentRepository;
        this.sellRepository = sellRepository;
        this.sellService = sellService;
        this.self = self;
        this.mailService = mailService;
        this.javaMailSender = javaMailSender;
    }

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

        String message = "주문서가 성공적으로 생성되었습니다.";
        try {
            messagingTemplate.convertAndSend("/topic/notifications", message);
        } catch (Exception e) {
            log.info("웹소켓 알림 실패");
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

        UserEntity manager = manager = userRepository.findByUsername(requestDto.getManagerId())
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
        try {
            messagingTemplate.convertAndSend("/topic/notifications", "주문서가 성공적으로 생성되었습니다.");
        } catch (Exception e) {
            log.info("웹소켓 알림 실패");
        }

        return orderRepository.save(order); // 저장 후 즉시 커밋
    }

    @Transactional(readOnly = true) // 이 어노테이션이 반드시 있어야 합니다.
    public List<OrderResponseDto> findAllOrders() {
        // 1. 페치 조인으로 엔티티 조회 (쿼리 1방)
        List<Order> orders = orderRepository.findAllWithFetchJoin();

        // 2. 엔티티 -> DTO 변환 (메모리 작업)
        return orders.stream()
                .map(OrderResponseDto::new) // 여기서 DTO로 변환
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
                            DELIVERYTRACKER_WEBHOOK_URL + "/delivery/webhook");

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

    public Order processOrderCancellation(Integer orderId) {

        boolean isRefunded = false;
        Optional<OrderPayment> paymentOpt = paymentRepository.findByOrder_OrderId(orderId);

        if (paymentOpt.isPresent()) {
            paymentLinkService.cancelPayment(orderId, "단순 변심에 의한 취소");
            isRefunded = true;
        }

        try {
            // 2. [내부 DB] 상태 변경 (트랜잭션 있음)
            return self.cancelOrderInDB(orderId);

        } catch (Exception e) {

            // 포트원 주문취소는 성공했는데, DB 반영 실패
            if (isRefunded) {
                log.error("🔥🔥 CRITICAL ERROR: 환불은 완료되었으나 DB 반영 실패! 수동 조치 필요. OrderId: {}", orderId);
                MimeMessage mail = mailService.createDbErrorMail("jack981109@gmail.com", orderId, e.getMessage());
                javaMailSender.send(mail);
            }

            throw new RuntimeException("주문 취소 처리 중 오류 발생 (환불 여부 확인 필요)", e);
        }
    }

    @Transactional
    public Order cancelOrderInDB(Integer orderId) {
        log.info("주문취소 시작, 주문Id: {}", orderId);

        // 주문상태 주문취소로 업데이트
        Order order = updateOrderStatus(orderId, OrderStatus.CANCELLED);

        // 가용재고 +1
        itemService.restoreItemAvailableQuantity(orderId);

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