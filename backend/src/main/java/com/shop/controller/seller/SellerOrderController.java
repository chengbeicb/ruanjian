package com.shop.controller.seller;

import com.shop.entity.Order;
import com.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/orders")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", maxAge = 3600)
public class SellerOrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 商家确认订单
     * PUT /api/seller/orders/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            Order order = orderService.confirmOrder(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单已确认");
            response.put("order", order);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 备货完成
     * PUT /api/seller/orders/{id}/prepare
     */
    @PutMapping("/{id}/prepare")
    public ResponseEntity<?> prepareOrder(@PathVariable Long id) {
        try {
            Order order = orderService.prepareOrder(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "备货完成");
            response.put("order", order);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 开始发货
     * PUT /api/seller/orders/{id}/ship
     */
    @PutMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        try {
            Order order = orderService.shipOrder(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已开始发货");
            response.put("order", order);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 交易完成
     * PUT /api/seller/orders/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            Order order = orderService.completeOrder(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "交易完成");
            response.put("order", order);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 商家取消订单
     * PUT /api/seller/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            Order order = orderService.cancelOrderBySeller(id, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单已取消");
            response.put("order", order);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
