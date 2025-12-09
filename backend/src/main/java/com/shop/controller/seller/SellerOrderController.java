package com.shop.controller.seller;

import com.shop.entity.Order;
import com.shop.repository.OrderRepository;
import com.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/orders")
// @CrossOrigin 已移除，由 SecurityConfig 全局配置 CORS
public class SellerOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 获取所有订单列表（商家）
     * GET /api/seller/orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreateTimeDesc();
        return ResponseEntity.ok(orders);
    }
    
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
