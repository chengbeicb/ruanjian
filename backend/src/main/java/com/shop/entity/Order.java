package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 订单编号（唯一）
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    // 客户
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // 订单状态：PENDING(客户下单)、CONFIRMED(商家确认)、PREPARING(备货完成)、SHIPPING(开始发货)、COMPLETED(交易完成)、CANCELLED(已取消)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    // 支付状态：UNPAID(待支付)、PAID(已支付)、REFUNDED(已退款)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    // 支付方式
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;
    
    // 支付时间
    private LocalDateTime paymentTime;
    
    // 订单总金额
    @Column(nullable = false)
    private Double totalAmount = 0.0;
    
    // 物流公司
    @Column(length = 50)
    private String logisticsCompany;
    
    // 物流单号
    @Column(length = 100)
    private String logisticsNumber;
    
    // 发货时间
    private LocalDateTime shippingTime;
    
    // 签收时间
    private LocalDateTime deliveryTime;
    
    // 收货人姓名
    private String receiverName;
    
    // 收货人电话
    private String receiverPhone;
    
    // 收货地址
    private String shippingAddress;
    
    // 订单备注
    @Column(length = 500)
    private String remark;
    
    // 订单商品明细
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // 创建时间
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    // 更新时间
    private LocalDateTime updateTime = LocalDateTime.now();
    
    // 取消时间
    private LocalDateTime cancelTime;
    
    // 取消原因
    private String cancelReason;
    
    // 取消人角色：CUSTOMER(客户)、SELLER(商家)
    @Enumerated(EnumType.STRING)
    private CancelRole cancelRole;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public CancelRole getCancelRole() {
        return cancelRole;
    }

    public void setCancelRole(CancelRole cancelRole) {
        this.cancelRole = cancelRole;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getLogisticsCompany() {
        return logisticsCompany;
    }

    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany;
    }

    public String getLogisticsNumber() {
        return logisticsNumber;
    }

    public void setLogisticsNumber(String logisticsNumber) {
        this.logisticsNumber = logisticsNumber;
    }

    public LocalDateTime getShippingTime() {
        return shippingTime;
    }

    public void setShippingTime(LocalDateTime shippingTime) {
        this.shippingTime = shippingTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    
    // 枚举：订单状态
    public enum OrderStatus {
        PENDING,      // 客户下单
        CONFIRMED,    // 商家确认
        PREPARING,    // 备货完成
        SHIPPING,     // 开始发货
        COMPLETED,    // 交易完成
        CANCELLED     // 已取消
    }
    
    // 枚举：取消人角色
    public enum CancelRole {
        CUSTOMER,     // 客户
        SELLER        // 商家
    }
    
    // 枚举：支付状态
    public enum PaymentStatus {
        UNPAID,       // 待支付
        PAID,         // 已支付
        REFUNDED      // 已退款
    }
    
    // 枚举：支付方式
    public enum PaymentMethod {
        BANK_CARD,      // 银行卡
        CREDIT_CARD,    // 信用卡
        ALIPAY,         // 支付宝
        WECHAT          // 微信支付
    }
}
