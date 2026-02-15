package com.pcgear.complink.pcgear.Sell;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                                .order(order)

                                // 고객 및 담당자 ID/Name
                                .customer(order.getCustomer())
                                .manager(order.getManager())

                                // 금액 정보 (Integer로 형 변환)
                                .totalAmount(order.getTotalAmount())
                                .vatAmount(order.getVatAmount())
                                .grandAmount(order.getGrandAmount())

                                // 판매 관련 고유 값 설정
                                .sellDate(LocalDateTime.now()) // 판매가 완료된 시점 (현재 시간)
                                .memo(null) // 초기 메모는 비워둠

                                .build();
        }

        public Page<SellResponseDto> getAllSells(Pageable pageable) {
                Page<Sell> sellPage = sellRepository.findAllWithDetails(pageable);

                return sellPage.map(SellResponseDto::from);
        }

        // 판매기록에 -매출 데이터 추가
        public void createNegateSell(Integer orderId) {
                log.info("판매기록에 -매출 데이터 추가");

                sellRepository.findByOrder_OrderId(orderId)
                                .ifPresent(sell -> {
                                        Sell newSell = Sell.builder()
                                                        .sellDate(LocalDateTime.now())
                                                        .customer(sell.getCustomer())
                                                        .manager(sell.getManager())
                                                        .vatAmount(sell.getVatAmount().negate())
                                                        .totalAmount(sell.getTotalAmount().negate())
                                                        .grandAmount(sell.getGrandAmount().negate())
                                                        .order(sell.getOrder())
                                                        .build();
                                        sellRepository.save(newSell);
                                });
        }

        /**
         * 보상 트랜잭션: 네거티브 매출 제거 (주문 취소 복구 시 사용)
         */
        public void removeNegateSell(Integer orderId) {
                log.info("네거티브 매출 제거 시작. OrderId: {}", orderId);

                // orderId에 해당하는 네거티브 매출 찾아서 삭제
                List<Sell> sells = sellRepository.findAllByOrder_OrderId(orderId);

                sells.stream()
                        .filter(sell -> sell.getGrandAmount().signum() < 0) // 음수인 매출만
                        .forEach(negativeSell -> {
                                log.info("네거티브 매출 삭제. SellId: {}, GrandAmount: {}",
                                        negativeSell.getSellId(), negativeSell.getGrandAmount());
                                sellRepository.delete(negativeSell);
                        });

                log.info("네거티브 매출 제거 완료. OrderId: {}", orderId);
        }

}
