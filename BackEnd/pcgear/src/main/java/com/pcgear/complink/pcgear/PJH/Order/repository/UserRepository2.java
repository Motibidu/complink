package com.pcgear.complink.pcgear.PJH.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Order.model.User;

@Repository
public interface UserRepository2 extends JpaRepository<User, Long> {
    // JpaRepository<엔티티 클래스, ID의 타입>
    // 이 인터페이스를 선언하는 것만으로, Spring Data JPA가
    // findById(), save(), findAll() 등의 메소드를 자동으로 생성해 줍니다.
}
