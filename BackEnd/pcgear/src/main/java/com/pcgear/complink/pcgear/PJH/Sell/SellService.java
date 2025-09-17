package com.pcgear.complink.pcgear.PJH.Sell;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.PJH.Order.model.Order;
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

        public Sell createSell(Sell sell) {
                Long orderId = sell.getOrderId();

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 주문서를 찾을 수 없습니다: " + orderId));

                // 3. 찾아온 주문서 객체의 상태를 '처리중'으로 변경합니다.
                order.setStatus("처리중");

                // 4. 변경된 주문서 상태를 데이터베이스에 저장(업데이트)합니다.
                // (@Transactional에 의해 메소드 종료 시 자동 반영되지만, 명시적으로 save 호출 가능)
                orderRepository.save(order);

                return sellRepository.save(sell);
        }

        public List<Sell> readSells() {
                return sellRepository.findAll();
        }

}
