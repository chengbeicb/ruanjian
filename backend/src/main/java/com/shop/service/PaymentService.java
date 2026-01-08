package com.shop.service;

import com.shop.entity.Order;
import com.shop.entity.Payment;
import com.shop.repository.OrderRepository;
import com.shop.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    // 创建支付记录
    @Transactional
    public Payment createPayment(Order order, Payment.PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentNumber("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentAmount(order.getTotalAmount());
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setCreateTime(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    // 模拟支付成功
    @Transactional
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        
        if (payment.getPaymentStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("支付状态异常");
        }
        
        // 模拟支付成功
        payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTransactionId("TXN" + System.currentTimeMillis());
        
        // 更新订单支付状态
        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaymentMethod(Order.PaymentMethod.valueOf(payment.getPaymentMethod().name()));
        order.setPaymentTime(payment.getPaymentTime());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);
        
        return paymentRepository.save(payment);
    }
    
    // 根据订单获取支付记录
    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order).orElse(null);
    }
    
    // 根据支付流水号获取支付记录
    public Payment getPaymentByNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber).orElse(null);
    }
}
