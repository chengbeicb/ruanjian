package com.shop.controller;

import com.shop.entity.Order;
import com.shop.entity.Payment;
import com.shop.repository.OrderRepository;
import com.shop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    // 创建支付
    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        String paymentMethodStr = request.get("paymentMethod").toString();
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.valueOf(paymentMethodStr);
        Payment payment = paymentService.createPayment(order, paymentMethod);
        
        return ResponseEntity.ok(payment);
    }
    
    // 模拟支付（点击支付按钮）
    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<Map<String, Object>> processPayment(@PathVariable Long paymentId) {
        Payment payment = paymentService.processPayment(paymentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "支付成功");
        response.put("payment", payment);
        
        return ResponseEntity.ok(response);
    }
    
    // 根据订单ID查询支付记录
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        Payment payment = paymentService.getPaymentByOrder(order);
        if (payment == null) {
            return ResponseEntity.ok(Collections.singletonMap("message", "未找到支付记录"));
        }
        
        return ResponseEntity.ok(payment);
    }
    
    // 根据支付流水号查询
    @GetMapping("/number/{paymentNumber}")
    public ResponseEntity<Payment> getPaymentByNumber(@PathVariable String paymentNumber) {
        Payment payment = paymentService.getPaymentByNumber(paymentNumber);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payment);
    }
}
