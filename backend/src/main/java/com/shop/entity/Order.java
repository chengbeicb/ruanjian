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
    
    // 订单总金额
    @Column(nullable = false)
    private Double totalAmount = 0.0;
    
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
}
