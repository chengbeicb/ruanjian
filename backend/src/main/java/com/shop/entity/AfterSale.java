package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "after_sales")
public class AfterSale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String afterSaleNumber;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceType serviceType;
    
    @Column(nullable = false, length = 50)
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AfterSaleStatus status = AfterSaleStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String sellerReply;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AfterSaleResult result;
    
    private Double refundAmount;
    
    @OneToMany(mappedBy = "afterSale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AfterSaleImage> images = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    private LocalDateTime updateTime = LocalDateTime.now();
    
    private LocalDateTime completeTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAfterSaleNumber() {
        return afterSaleNumber;
    }

    public void setAfterSaleNumber(String afterSaleNumber) {
        this.afterSaleNumber = afterSaleNumber;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AfterSaleStatus getStatus() {
        return status;
    }

    public void setStatus(AfterSaleStatus status) {
        this.status = status;
    }

    public String getSellerReply() {
        return sellerReply;
    }

    public void setSellerReply(String sellerReply) {
        this.sellerReply = sellerReply;
    }

    public AfterSaleResult getResult() {
        return result;
    }

    public void setResult(AfterSaleResult result) {
        this.result = result;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public List<AfterSaleImage> getImages() {
        return images;
    }

    public void setImages(List<AfterSaleImage> images) {
        this.images = images;
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

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }
    
    // 枚举：服务类型
    public enum ServiceType {
        RETURN,         // 退货
        REFUND,         // 退款
        EXCHANGE        // 换货
    }
    
    // 枚举：处理状态
    public enum AfterSaleStatus {
        PENDING,        // 待处理
        PROCESSING,     // 处理中
        APPROVED,       // 已同意
        REJECTED,       // 已拒绝
        COMPLETED       // 已完成
    }
    
    // 枚举：处理结果
    public enum AfterSaleResult {
        REFUNDED,       // 已退款
        RETURNED,       // 已退货
        EXCHANGED       // 已换货
    }
}
