package com.pcgear.complink.pcgear.Delivery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Delivery.entity.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
        List<Delivery> findAllByTrackingNumber(String trackingNumber);

        Delivery findByTrackingNumber(String trackingNumber);

        /**
         * 웹훅 등록이 PENDING 상태인 배송 정보 조회
         */
        // @Query("SELECT d FROM Delivery d WHERE d.webhookStatus = com.pcgear.complink.pcgear.Delivery.enums.DeliveryWebhookStatus.PENDING AND d.webhookRetryCount < 10")
        // List<Delivery> findPendingWebhookRegistrations();

        /**
         * 웹훅 등록이 FAILED 상태인 배송 정보 조회 (모니터링용)
         */
        // @Query("SELECT d FROM Delivery d WHERE d.webhookStatus = com.pcgear.complink.pcgear.Delivery.enums.DeliveryWebhookStatus.FAILED")
        // List<Delivery> findFailedWebhookRegistrations();

        
}
