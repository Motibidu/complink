package com.pcgear.complink.pcgear.PJH.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Order.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // JpaRepository<엔티티 클래스, ID 타입>
    // 이 인터페이스만으로 save(), findById(), findAll() 등의 메소드를 사용할 수 있습니다.
}
