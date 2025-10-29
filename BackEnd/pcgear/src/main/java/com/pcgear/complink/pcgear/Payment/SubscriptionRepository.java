package com.pcgear.complink.pcgear.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
        Optional<Subscription> findByTrackingId(String trackingId);
}
