package com.pcgear.complink.pcgear.Sell;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcgear.complink.pcgear.Item.Item;
import com.pcgear.complink.pcgear.Item.ItemRepository;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.Order.model.OrderItem;
import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellService {
        private final SellRepository sellRepository;
        private final OrderRepository orderRepository;
        private final ItemRepository itemRepository; // 품목 재고 관리를 위해 추가

        // 1. 주문 상태를 'PAID'로 변경
        // 2. 재고 수량 변경
        // 3. Sell 데이터 생성
        @CacheEvict(value = "dashboard-summary", allEntries = true)
        @Transactional
        public Sell createSell(Order order) {
                // 3. 판매(Sell) 데이터 생성
                Sell newSell = mapOrderToSell(order);

                return sellRepository.save(newSell);
        }

        public List<Sell> readSells() {
                return sellRepository.findAll();
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
