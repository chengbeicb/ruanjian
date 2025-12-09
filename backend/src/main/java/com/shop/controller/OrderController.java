package com.shop.controller;

import com.shop.entity.Order;
import com.shop.entity.Customer;
import com.shop.repository.CustomerRepository;
import com.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
// @CrossOrigin 已移除，由 SecurityConfig 全局配置 CORS
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * 创建订单（从购物车）
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(Authentication authentication, @RequestBody Map<String, Object> request) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            // 处理 cartIds - JSON 解析时数字可能是 Integer，需要转换为 Long
            @SuppressWarnings("unchecked")
            List<?> rawCartIds = (List<?>) request.get("cartIds");
            List<Long> cartIds = rawCartIds.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return Long.parseLong(id.toString());
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            String receiverName = (String) request.get("receiverName");
            String receiverPhone = (String) request.get("receiverPhone");
            String shippingAddress = (String) request.get("shippingAddress");
            String remark = (String) request.get("remark");
            
            Order order = orderService.createOrderFromCart(customer, cartIds, receiverName, 
                                                          receiverPhone, shippingAddress, remark);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单创建成功");
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
     * 获取客户订单列表
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getOrders(Authentication authentication) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            List<Order> orders = orderService.getCustomerOrders(customer);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 客户取消订单
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(Authentication authentication, @PathVariable Long id, 
                                        @RequestBody Map<String, String> request) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            String reason = request.get("reason");
            Order order = orderService.cancelOrderByCustomer(id, customer.getId(), reason);
            
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
