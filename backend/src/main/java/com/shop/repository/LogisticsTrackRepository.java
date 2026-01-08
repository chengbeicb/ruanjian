package com.shop.repository;

import com.shop.entity.LogisticsInfo;
import com.shop.entity.LogisticsTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogisticsTrackRepository extends JpaRepository<LogisticsTrack, Long> {
    
    // 查询物流信息的所有跟踪记录
    List<LogisticsTrack> findByLogisticsInfoOrderByTrackTimeDesc(LogisticsInfo logisticsInfo);
}
