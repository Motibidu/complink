package com.pcgear.complink.pcgear.Dashboard;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopItemsCacheService {

    private final EntityManager entityManager;
    private final TopItemSalesCacheRepository cacheRepository;

    /**
     * 30분마다 TOP 10 판매 아이템 캐시 갱신
     */
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void refreshTopItemsCache() {
        log.info("Starting TOP 10 items sales cache refresh");

        try {
            // 1. 기존 캐시 삭제
            cacheRepository.deleteAll();
            entityManager.flush();

            // 2. 집계 쿼리 실행 후 캐시 테이블에 삽입
            String sql = """
                    INSERT INTO top_items_sales_cache
                    (item_id, item_name, total_quantity, total_revenue, available_quantity, rank_position, cached_at)
                    SELECT
                        i.item_id,
                        i.item_name,
                        SUM(oi.quantity) as total_quantity,
                        CAST(SUM(oi.quantity * oi.unit_price) AS SIGNED) as total_revenue,
                        i.available_quantity,
                        ROW_NUMBER() OVER (ORDER BY SUM(oi.quantity) DESC) as rank_position,
                        NOW()
                    FROM order_items oi
                    JOIN items i ON i.item_id = oi.item_id
                    JOIN orders o ON o.order_id = oi.order_id
                    WHERE o.order_status IN ('PAID', 'PREPARING_PRODUCT', 'SHIPPING_PENDING', 'SHIPPING', 'DELIVERED')
                    GROUP BY i.item_id, i.item_name, i.available_quantity
                    ORDER BY total_quantity DESC
                    LIMIT 10
                    """;

            int insertedRows = entityManager.createNativeQuery(sql).executeUpdate();
            log.info("TOP 10 items sales cache refreshed successfully. Inserted {} rows", insertedRows);

        } catch (Exception e) {
            log.error("Failed to refresh TOP 10 items sales cache", e);
            throw e;
        }
    }

    /**
     * 수동으로 캐시 갱신 (API 호출용)
     */
    @Transactional
    public void manualRefresh() {
        log.info("Manual refresh requested");
        refreshTopItemsCache();
    }
}
