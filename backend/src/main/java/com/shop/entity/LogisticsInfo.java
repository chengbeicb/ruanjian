package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logistics_info")
public class LogisticsInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false, length = 50)
    private String logisticsCompany;
    
    @Column(nullable = false, length = 100)
    private String logisticsNumber;
    
    @Column(length = 50)
    private String currentStatus;
    
    @OneToMany(mappedBy = "logisticsInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LogisticsTrack> tracks = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    private LocalDateTime updateTime = LocalDateTime.now();

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

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public List<LogisticsTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<LogisticsTrack> tracks) {
        this.tracks = tracks;
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
