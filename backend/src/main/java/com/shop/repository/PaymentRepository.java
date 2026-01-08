package com.shop.repository;

import com.shop.entity.Order;
import com.shop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // 根据订单查询支付记录
    Optional<Payment> findByOrder(Order order);
    
    // 根据支付流水号查询
    Optional<Payment> findByPaymentNumber(String paymentNumber);
}
