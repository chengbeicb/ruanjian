package com.shop.controller;

import com.shop.entity.AfterSale;
import com.shop.entity.AfterSaleImage;
import com.shop.entity.Customer;
import com.shop.service.AfterSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/after-sales")
public class AfterSaleController {
    
    @Autowired
    private AfterSaleService afterSaleService;
    
    // 创建售后申请
    @PostMapping
    public ResponseEntity<AfterSale> createAfterSale(@RequestBody Map<String, Object> request,
                                                     Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        
        Long orderItemId = Long.valueOf(request.get("orderItemId").toString());
        AfterSale.ServiceType serviceType = AfterSale.ServiceType.valueOf(request.get("serviceType").toString());
        String reason = request.get("reason").toString();
        String description = request.getOrDefault("description", "").toString();
        
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) request.get("imageUrls");
        
        AfterSale afterSale = afterSaleService.createAfterSale(customer, orderItemId, serviceType, 
                                                               reason, description, imageUrls);
        
        return ResponseEntity.ok(afterSale);
    }
    
    // 获取客户的售后申请列表
    @GetMapping
    public ResponseEntity<List<AfterSale>> getMyAfterSales(Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        List<AfterSale> afterSales = afterSaleService.getAfterSalesByCustomer(customer);
        return ResponseEntity.ok(afterSales);
    }
    
    // 获取售后申请详情
    @GetMapping("/{id}")
    public ResponseEntity<AfterSale> getAfterSale(@PathVariable Long id) {
        AfterSale afterSale = afterSaleService.getAfterSaleById(id);
        return ResponseEntity.ok(afterSale);
    }
    
    // 获取售后申请的图片列表
    @GetMapping("/{id}/images")
    public ResponseEntity<List<AfterSaleImage>> getAfterSaleImages(@PathVariable Long id) {
        List<AfterSaleImage> images = afterSaleService.getAfterSaleImages(id);
        return ResponseEntity.ok(images);
    }
}
