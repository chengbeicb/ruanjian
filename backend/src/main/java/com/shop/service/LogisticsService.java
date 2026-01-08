package com.shop.service;

import com.shop.entity.LogisticsInfo;
import com.shop.entity.LogisticsTrack;
import com.shop.entity.Order;
import com.shop.repository.LogisticsInfoRepository;
import com.shop.repository.LogisticsTrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogisticsService {
    
    @Autowired
    private LogisticsInfoRepository logisticsInfoRepository;
    
    @Autowired
    private LogisticsTrackRepository logisticsTrackRepository;
    
    // 创建物流信息
    @Transactional
    public LogisticsInfo createLogistics(Order order, String logisticsCompany, String logisticsNumber) {
        LogisticsInfo logisticsInfo = new LogisticsInfo();
        logisticsInfo.setOrder(order);
        logisticsInfo.setLogisticsCompany(logisticsCompany);
        logisticsInfo.setLogisticsNumber(logisticsNumber);
        logisticsInfo.setCurrentStatus("已发货");
        logisticsInfo.setCreateTime(LocalDateTime.now());
        logisticsInfo.setUpdateTime(LocalDateTime.now());
        
        logisticsInfo = logisticsInfoRepository.save(logisticsInfo);
        
        // 添加第一条物流跟踪记录
        addLogisticsTrack(logisticsInfo, "已发货", "商品已发货，等待快递公司揽件", "发货地");
        
        return logisticsInfo;
    }
    
    // 添加物流跟踪记录
    @Transactional
    public LogisticsTrack addLogisticsTrack(LogisticsInfo logisticsInfo, String status, String info, String location) {
        LogisticsTrack track = new LogisticsTrack();
        track.setLogisticsInfo(logisticsInfo);
        track.setTrackTime(LocalDateTime.now());
        track.setTrackStatus(status);
        track.setTrackInfo(info);
        track.setLocation(location);
        track.setCreateTime(LocalDateTime.now());
        
        // 更新物流信息的当前状态
        logisticsInfo.setCurrentStatus(status);
        logisticsInfo.setUpdateTime(LocalDateTime.now());
        logisticsInfoRepository.save(logisticsInfo);
        
        return logisticsTrackRepository.save(track);
    }
    
    // 模拟添加物流跟踪记录（用于测试）
    @Transactional
    public void simulateLogisticsProgress(Long logisticsInfoId) {
        LogisticsInfo logisticsInfo = logisticsInfoRepository.findById(logisticsInfoId)
                .orElseThrow(() -> new RuntimeException("物流信息不存在"));
        
        // 模拟物流进度
        addLogisticsTrack(logisticsInfo, "运输中", "快递已被揽收", "北京市朝阳区");
        addLogisticsTrack(logisticsInfo, "运输中", "快递正在运输途中", "河北省廊坊市");
        addLogisticsTrack(logisticsInfo, "派送中", "快递已到达目的地，正在派送", "上海市浦东新区");
    }
    
    // 根据订单获取物流信息
    public LogisticsInfo getLogisticsByOrder(Order order) {
        return logisticsInfoRepository.findByOrder(order).orElse(null);
    }
    
    // 获取物流跟踪记录
    public List<LogisticsTrack> getLogisticsTracks(Long logisticsInfoId) {
        LogisticsInfo logisticsInfo = logisticsInfoRepository.findById(logisticsInfoId)
                .orElseThrow(() -> new RuntimeException("物流信息不存在"));
        return logisticsTrackRepository.findByLogisticsInfoOrderByTrackTimeDesc(logisticsInfo);
    }
}
