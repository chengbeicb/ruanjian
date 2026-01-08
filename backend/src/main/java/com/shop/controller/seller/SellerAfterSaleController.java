package com.shop.controller.seller;

import com.shop.entity.AfterSale;
import com.shop.service.AfterSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/after-sales")
public class SellerAfterSaleController {
    
    @Autowired
    private AfterSaleService afterSaleService;
    
    // 获取所有售后申请
    @GetMapping
    public ResponseEntity<List<AfterSale>> getAllAfterSales() {
        List<AfterSale> afterSales = afterSaleService.getAllAfterSales();
        return ResponseEntity.ok(afterSales);
    }
    
    // 获取售后申请详情
    @GetMapping("/{id}")
    public ResponseEntity<AfterSale> getAfterSale(@PathVariable Long id) {
        AfterSale afterSale = afterSaleService.getAfterSaleById(id);
        return ResponseEntity.ok(afterSale);
    }
    
    // 处理售后申请（同意/拒绝）
    @PutMapping("/{id}/process")
    public ResponseEntity<AfterSale> processAfterSale(@PathVariable Long id,
                                                      @RequestBody Map<String, String> request) {
        String statusStr = request.get("status");
        String sellerReply = request.getOrDefault("sellerReply", "");
        
        AfterSale.AfterSaleStatus status = AfterSale.AfterSaleStatus.valueOf(statusStr);
        AfterSale afterSale = afterSaleService.processAfterSale(id, status, sellerReply);
        
        return ResponseEntity.ok(afterSale);
    }
    
    // 完成售后申请
    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeAfterSale(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> request) {
        String resultStr = request.get("result");
        AfterSale.AfterSaleResult result = AfterSale.AfterSaleResult.valueOf(resultStr);
        
        AfterSale afterSale = afterSaleService.completeAfterSale(id, result);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "售后处理完成");
        response.put("afterSale", afterSale);
        
        return ResponseEntity.ok(response);
    }
}
