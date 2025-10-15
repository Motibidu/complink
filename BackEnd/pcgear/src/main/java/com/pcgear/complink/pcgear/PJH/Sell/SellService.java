package com.pcgear.complink.pcgear.PJH.Sell;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.PJH.Order.model.Order;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderItem;
import com.pcgear.complink.pcgear.PJH.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.PJH.Order.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellService {
        private final SellRepository sellRepository;
        private final OrderRepository orderRepository;

        // order의 orderStatus를 OrderStatus.PAID로 업데이트하고, sell 저장
        public Sell createSellAndUpdateToPaid(Integer orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 주문서를 찾을 수 없습니다: " + orderId));

                order.setOrderStatus(OrderStatus.PAID);
                orderRepository.save(order);

                List<OrderItem> itemsFromOrder = order.getItems();

                for(OrderItem orderItem: itemsFromOrder){
                        
                }

                Sell newSell = mapOrderToSell(order);

                return sellRepository.save(newSell);
        }

        public List<Sell> readSells() {
                return sellRepository.findAll();
        }

        private void updateItemQuantityOnHand() {

        }

        private Sell mapOrderToSell(Order order) {
                return Sell.builder()
                                .orderId(order.getOrderId())

                                // 고객 및 담당자 ID/Name
                                .customerId(order.getCustomer().getCustomerId())
                                .customerName(order.getCustomer().getCustomerName())
                                .managerId(order.getManager().getManagerId())
                                .managerName(order.getManager().getManagerName())

                                // 금액 정보 (Integer로 형 변환)
                                .totalAmount(order.getTotalAmount())
                                .vatAmount(order.getVatAmount())
                                .grandAmount(order.getGrandAmount())

                                // 판매 관련 고유 값 설정
                                .sellDate(LocalDateTime.now()) // 판매가 완료된 시점 (현재 시간)
                                .memo(null) // 초기 메모는 비워둠

                                .build();
        }

}
