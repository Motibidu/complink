package com.pcgear.complink.pcgear.PJH.Delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Delivery.model.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

}
