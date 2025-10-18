package com.pcgear.complink.pcgear.Delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Delivery.model.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

        boolean existsByOrderId(Integer orderId);

        Delivery findByTrackingNumber(String trackingNumber);

        Optional<Delivery> findByOrderId(Integer orderId);

        List<Delivery> findAllByTrackingNumber(String trackingNumber);
}
