package com.pcgear.complink.pcgear.PJH.Order.service;

import com.pcgear.complink.pcgear.PJH.Order.model.Customer;
import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderItem;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderRequestDto;
import com.pcgear.complink.pcgear.PJH.Order.model.User;
import com.pcgear.complink.pcgear.PJH.Order.repository.CustomerRepository;
import com.pcgear.complink.pcgear.PJH.Order.repository.OrderRepository;
import com.pcgear.complink.pcgear.KJG.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    // @Transactional
    // public Order createOrder(OrderRequestDto requestDto) {
    //     // 1. ID를 사용하여 User와 Customer 엔티티를 조회합니다.
    //     // FIXED: 중괄호와 return을 생략하여 예외 객체를 올바르게 반환합니다.
    //     Customer customer = customerRepository.findById(requestDto.getCustomerId())
    //             .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다. ID: " + requestDto.getCustomerId()));

    //     User manager = null;
    //     if (requestDto.getManagerId() != null) {
    //         // FIXED: 여기도 마찬가지로 수정합니다.
    //         manager = userRepository.findById(requestDto.getManagerId())
    //                 .orElseThrow(() -> new EntityNotFoundException("담당자 정보를 찾을 수 없습니다. ID: " + requestDto.getManagerId()));
    //     }

    //     // 2. DTO -> Entity 변환
    //     Order order = new Order();
    //     order.setOrderDate(requestDto.getOrderDate());
    //     order.setDeliveryDate(requestDto.getDeliveryDate());
    //     order.setStatus(requestDto.getStatus());
        
    //     order.setCustomer(customer);
    //     order.setManager(manager);

    //     order.setTotalAmount(requestDto.getTotalAmount());
    //     order.setVatAmount(requestDto.getVatAmount());
    //     order.setGrandTotal(requestDto.getGrandTotal());

    //     // 3. 주문 아이템 리스트 변환 및 추가
    //     for (OrderRequestDto.OrderItemDto itemDto : requestDto.getItems()) {
    //         OrderItem orderItem = new OrderItem();
    //         orderItem.setPartNumber(itemDto.getPartNumber());
    //         orderItem.setItemName(itemDto.getItemName());
    //         orderItem.setQuantity(itemDto.getQuantity());
    //         orderItem.setUnitPrice(itemDto.getUnitPrice());
    //         orderItem.setItemTotal(itemDto.getItemTotal());
            
    //         order.addItem(orderItem);
    //     }

    //     // 4. Repository를 통해 DB에 저장
    //     return orderRepository.save(order);
    // }
}