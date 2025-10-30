package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description; // 支持富媒体描述
    
    private Double price;
    
    // 多图片支持，使用逗号分隔的图片URL列表
    @Column(columnDefinition = "TEXT")
    private String imageUrls;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;
    
    // 库存数量
    private Integer stockQuantity;
    
    // 一级分类
    private String categoryLevel1;
    
    // 二级分类
    private String categoryLevel2;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    public enum ProductStatus {
        AVAILABLE, // 可购买
        SOLD,      // 已售出
        FROZEN,    // 冻结中（有人正在购买）
        UNPUBLISHED // 新添加：已下架
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public String getCategoryLevel1() {
        return categoryLevel1;
    }
    
    public void setCategoryLevel1(String categoryLevel1) {
        this.categoryLevel1 = categoryLevel1;
    }
    
    public String getCategoryLevel2() {
        return categoryLevel2;
    }
    
    public void setCategoryLevel2(String categoryLevel2) {
        this.categoryLevel2 = categoryLevel2;
    }
    
    public ProductStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProductStatus status) {
        this.status = status;
    }
    
    public Seller getSeller() {
        return seller;
    }
    
    public void setSeller(Seller seller) {
        this.seller = seller;
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
}