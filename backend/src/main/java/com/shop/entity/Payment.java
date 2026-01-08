package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(unique = true, nullable = false, length = 100)
    private String paymentNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @Column(nullable = false)
    private Double paymentAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    private LocalDateTime paymentTime;
    
    @Column(length = 100)
    private String transactionId;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    // 枚举：支付方式
    public enum PaymentMethod {
        BANK_CARD,      // 银行卡
        CREDIT_CARD,    // 信用卡
        ALIPAY,         // 支付宝
        WECHAT          // 微信支付
    }
    
    // 枚举：支付状态
    public enum PaymentStatus {
        PENDING,        // 处理中
        SUCCESS,        // 成功
        FAILED          // 失败
    }
}
