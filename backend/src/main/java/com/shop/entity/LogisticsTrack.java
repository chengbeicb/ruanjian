package com.shop.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logistics_tracks")
public class LogisticsTrack {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "logistics_info_id", nullable = false)
    private LogisticsInfo logisticsInfo;
    
    @Column(nullable = false)
    private LocalDateTime trackTime;
    
    @Column(nullable = false, length = 50)
    private String trackStatus;
    
    @Column(nullable = false, length = 500)
    private String trackInfo;
    
    @Column(length = 100)
    private String location;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LogisticsInfo getLogisticsInfo() {
        return logisticsInfo;
    }

    public void setLogisticsInfo(LogisticsInfo logisticsInfo) {
        this.logisticsInfo = logisticsInfo;
    }

    public LocalDateTime getTrackTime() {
        return trackTime;
    }

    public void setTrackTime(LocalDateTime trackTime) {
        this.trackTime = trackTime;
    }

    public String getTrackStatus() {
        return trackStatus;
    }

    public void setTrackStatus(String trackStatus) {
        this.trackStatus = trackStatus;
    }

    public String getTrackInfo() {
        return trackInfo;
    }

    public void setTrackInfo(String trackInfo) {
        this.trackInfo = trackInfo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
