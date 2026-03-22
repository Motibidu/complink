package com.pcgear.complink.pcgear.Delivery;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pcgear.complink.pcgear.Delivery.entity.DeliveryWebhookRegisterRetry;
import com.pcgear.complink.pcgear.Delivery.enums.RetryStatus;

@Repository
public interface DeliveryWebhookResisgterRepository extends JpaRepository<DeliveryWebhookRegisterRetry, Long> {

        List<DeliveryWebhookRegisterRetry> findAllByStatusAndNextRunAtBefore(RetryStatus status,
                        LocalDateTime nextRunAt);

        DeliveryWebhookRegisterRetry  findByTargetId(Long targetId);


}
