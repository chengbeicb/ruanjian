package com.shop.entity;

import javax.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 所属订单
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    // 商品
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // 购买数量
    @Column(nullable = false)
    private Integer quantity;
    
    // 商品单价（下单时的价格，防止后续商品价格变化）
    @Column(nullable = false)
    private Double unitPrice;
    
    // 小计金额
    @Column(nullable = false)
    private Double subtotal;

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
