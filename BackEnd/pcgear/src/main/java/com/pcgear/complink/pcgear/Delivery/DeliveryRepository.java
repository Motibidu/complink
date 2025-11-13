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

        // @Query("SELECT new com.pcgear.complink.pcgear.Delivery.model.ShippingListDto(" +
        //                 " o.orderId, " +
        //                 " o.customer.customerName, " + // Order -> Customer JOIN
        //                 " d.createdAt, " +
        //                 " d.trackingNumber, " + // Order -> Delivery JOIN (수동)
        //                 " d.carrierId, " +
        //                 " d.deliveryStatus " +
        //                 ") " +
        //                 "FROM Order o " +
        //                 "JOIN o.customer c " + // (o.customer는 매핑되어 있다고 가정)
        //                 "JOIN Delivery d ON d.orderId = o.orderId ")
        // Page<ShippingListDto> findShippingList(Pageable pageable);
}
