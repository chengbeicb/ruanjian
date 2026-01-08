package com.shop.controller;

import com.shop.entity.LogisticsInfo;
import com.shop.entity.LogisticsTrack;
import com.shop.entity.Order;
import com.shop.repository.OrderRepository;
import com.shop.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {
    
    @Autowired
    private LogisticsService logisticsService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    // 根据订单ID查询物流信息
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getLogisticsByOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        LogisticsInfo logisticsInfo = logisticsService.getLogisticsByOrder(order);
        if (logisticsInfo == null) {
            return ResponseEntity.ok(Collections.singletonMap("message", "暂无物流信息"));
        }
        
        return ResponseEntity.ok(logisticsInfo);
    }
    
    // 查询物流跟踪记录
    @GetMapping("/{logisticsInfoId}/tracks")
    public ResponseEntity<List<LogisticsTrack>> getLogisticsTracks(@PathVariable Long logisticsInfoId) {
        List<LogisticsTrack> tracks = logisticsService.getLogisticsTracks(logisticsInfoId);
        return ResponseEntity.ok(tracks);
    }
    
    // 模拟物流进度更新（测试用）
    @PostMapping("/{logisticsInfoId}/simulate")
    public ResponseEntity<Map<String, String>> simulateLogistics(@PathVariable Long logisticsInfoId) {
        logisticsService.simulateLogisticsProgress(logisticsInfoId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "物流进度模拟成功");
        return ResponseEntity.ok(response);
    }
}
