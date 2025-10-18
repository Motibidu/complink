package com.pcgear.complink.pcgear.Order.service;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Customer.CustomerRepository;
import com.pcgear.complink.pcgear.Item.ItemRepository;
import com.pcgear.complink.pcgear.Manager.Manager;
import com.pcgear.complink.pcgear.Manager.ManagerRepository;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderItem;
import com.pcgear.complink.pcgear.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.Payment.PaymentLinkService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ManagerRepository managerRepository;
    private final CustomerRepository customerRepository;
    private final PaymentLinkService paymentLinkService;

    private final SimpMessagingTemplate messagingTemplate;

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
            String merchantUid = "orderId:" + orderRepository.count();
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
            orderItem.setItemId(itemDto.getItemId());
            orderItem.setCategory(itemDto.getCategory());
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

    public void deleteOrder(Integer orderId) {
        orderRepository.deleteById(orderId);
    }

    public List<OrderResponseDto> findByOrderStatus(OrderStatus orderStatus) {
        return orderRepository.findByOrderStatus(orderStatus).stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    public Order updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다. ID: " + orderId));

        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }
}