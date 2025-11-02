package com.pcgear.complink.pcgear.Delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Delivery.entity.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
        List<Delivery> findAllByTrackingNumber(String trackingNumber);

        Delivery findByTrackingNumber(String trackingNumber);
}
