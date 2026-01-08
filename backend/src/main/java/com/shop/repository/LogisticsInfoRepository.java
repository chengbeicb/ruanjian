package com.shop.repository;

import com.shop.entity.LogisticsInfo;
import com.shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogisticsInfoRepository extends JpaRepository<LogisticsInfo, Long> {
    
    // 根据订单查询物流信息
    Optional<LogisticsInfo> findByOrder(Order order);
    
    // 根据物流单号查询
    Optional<LogisticsInfo> findByLogisticsNumber(String logisticsNumber);
}
