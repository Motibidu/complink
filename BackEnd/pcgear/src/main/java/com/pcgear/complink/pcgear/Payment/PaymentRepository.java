package com.pcgear.complink.pcgear.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<OrderPayment, String> {

        Optional<OrderPayment> findByPaymentId(String paymentId);

        Optional<OrderPayment> findByOrder_OrderId(Integer orderId);

}
