package com.agency.paypal.repository;

import com.agency.paypal.model.PayPalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayPalRepository extends JpaRepository <PayPalTransaction,Long> {
    PayPalTransaction findByPaymentId(String paymentId);
}
