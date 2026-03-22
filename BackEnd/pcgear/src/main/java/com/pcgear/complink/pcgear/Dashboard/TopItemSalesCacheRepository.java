package com.pcgear.complink.pcgear.Dashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopItemSalesCacheRepository extends JpaRepository<TopItemSalesCache, Long> {

    @Query("SELECT t FROM TopItemSalesCache t ORDER BY t.rankPosition ASC")
    List<TopItemSalesCache> findTop10ByOrderByRankPosition();
}
