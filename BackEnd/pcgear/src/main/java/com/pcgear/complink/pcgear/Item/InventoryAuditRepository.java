package com.pcgear.complink.pcgear.Item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {

    /**
     * 특정 품목의 감사 로그 조회
     */
    List<InventoryAudit> findByItemOrderByCreatedAtDesc(Item item);

    /**
     * 기간별 감사 로그 조회
     */
    //List<InventoryAudit> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
