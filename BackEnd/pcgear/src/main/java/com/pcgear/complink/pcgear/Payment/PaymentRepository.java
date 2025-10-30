package com.pcgear.complink.pcgear.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

        Optional<Payment> findByPaymentId(String paymentId);

}
