package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "after_sale_images")
public class AfterSaleImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "after_sale_id", nullable = false)
    private AfterSale afterSale;
    
    @Column(nullable = false, length = 500)
    private String imageUrl;
    
    @Column
    private Integer imageOrder = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AfterSale getAfterSale() {
        return afterSale;
    }

    public void setAfterSale(AfterSale afterSale) {
        this.afterSale = afterSale;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getImageOrder() {
        return imageOrder;
    }

    public void setImageOrder(Integer imageOrder) {
        this.imageOrder = imageOrder;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
