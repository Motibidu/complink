package com.pcgear.complink.pcgear.PJH.Order.service;

import com.pcgear.complink.pcgear.PJH.Order.model.Customer;
import com.pcgear.complink.pcgear.PJH.Order.model.Manager;
import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderItem;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderResponseDto;
import com.pcgear.complink.pcgear.PJH.Order.repository.CustomerRepository;
import com.pcgear.complink.pcgear.PJH.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.PJH.Order.repository.ManagerRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ManagerRepository managerRepository;
    private final CustomerRepository customerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderService(OrderRepository orderRepository, ManagerRepository managerRepository,
            CustomerRepository customerRepository, SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.managerRepository = managerRepository;
        this.customerRepository = customerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Order createOrder(OrderRequestDto requestDto) {
        // 1. ID를 사용하여 User와 Customer 엔티티를 조회합니다.
        // FIXED: 중괄호와 return을 생략하여 예외 객체를 올바르게 반환합니다.
        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("거래처 정보를 찾을 수 없습니다. ID: " + requestDto.getCustomerId()));

        Manager manager = null;
        if (requestDto.getManagerName() != null) {
            // FIXED: 여기도 마찬가지로 수정합니다.
            manager = managerRepository.findById(requestDto.getManagerId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("담당자 정보를 찾을 수 없습니다. ID: " + requestDto.getManagerId()));
        }

        // 2. DTO -> Entity 변환
        Order order = new Order();
        order.setOrderDate(requestDto.getOrderDate());
        order.setDeliveryDate(requestDto.getDeliveryDate());
        order.setStatus(requestDto.getStatus());

        order.setCustomer(customer);
        order.setManager(manager);

        order.setTotalAmount(requestDto.getTotalAmount());
        order.setVatAmount(requestDto.getVatAmount());
        order.setGrandAmount(requestDto.getGrandAmount());

        // 3. 주문 아이템 리스트 변환 및 추가
        for (OrderRequestDto.OrderItemDto itemDto : requestDto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemName(itemDto.getItemName());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(itemDto.getUnitPrice());
            orderItem.setTotalPrice(itemDto.getTotalPrice());

            order.addItem(orderItem);
        }

        String message = "주문서가 성공적으로 생성되었습니다.";
        // "/topic/notifications" 토픽을 구독하는 클라이언트에게 메시지를 보냄
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

    public List<Customer> findAllCustomers(){
        return customerRepository.findAll();
    }

    public List<Manager> findAllManagers(){
        return managerRepository.findAll();
    }
}