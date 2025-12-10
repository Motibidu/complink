package com.pcgear.complink.pcgear.Sell;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellRepository extends JpaRepository<Sell, Integer> {
        @Query("SELECT COALESCE(SUM(s.grandAmount), 0) FROM Sell s " +
                        "WHERE s.createdAt BETWEEN :startOfDay AND :endOfDay")
        Integer getTodayTotalSales(
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        Optional<Sell> findByOrder_OrderId(Integer orderid);

        @Query("SELECT s FROM Sell s " +
                        "JOIN FETCH s.order " +
                        "LEFT JOIN FETCH s.customer " +
                        "LEFT JOIN FETCH s.manager ")
        Page<Sell> findAllWithDetails(Pageable pageable);

}
