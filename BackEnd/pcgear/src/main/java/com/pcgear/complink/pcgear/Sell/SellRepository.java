package com.pcgear.complink.pcgear.Sell;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellRepository extends JpaRepository<Sell, Integer> {
        @Query("SELECT COALESCE(SUM(s.grandAmount), 0) FROM Sell s " +
                        "WHERE s.createdAt BETWEEN :startOfDay AND :endOfDay")
        Integer countBySellDateBetween(
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);
}
