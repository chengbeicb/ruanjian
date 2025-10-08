package com.shop.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
//@Entity
public class PurchaseIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String buyerName;
    private String buyerPhone;
    private String buyerEmail;
    private String buyerAddress;
    private LocalDateTime createTime;
    private boolean completed = false;
    private boolean canceled = false;
    
    @OneToOne
    private Product product;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}