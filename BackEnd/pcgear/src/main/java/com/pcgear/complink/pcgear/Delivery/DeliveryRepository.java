package com.pcgear.complink.pcgear.Delivery;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.Delivery.entity.Delivery;
import com.pcgear.complink.pcgear.Delivery.model.ShippingListDto;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
        List<Delivery> findAllByTrackingNumber(String trackingNumber);

        Delivery findByTrackingNumber(String trackingNumber);
}
