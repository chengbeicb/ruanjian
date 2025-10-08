package com.shop.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
//@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private String imageUrl;
    private double price;
    private boolean available = true; // 是否可购买
    private boolean frozen = false; // 是否冻结（意向购买中）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @ManyToOne
    private Seller seller;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}