package com.pcgear.complink.pcgear.PJH.Delivery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Delivery.model.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

        boolean existsByOrderId(Integer orderId);

        Delivery findByTrackingNumber(String trackingNumber);

        List<Delivery> findAllByTrackingNumber(String trackingNumber);
}
