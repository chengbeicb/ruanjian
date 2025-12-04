package com.shop.controller;

import com.shop.entity.ShoppingCart;
import com.shop.entity.Customer;
import com.shop.repository.CustomerRepository;
import com.shop.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", maxAge = 3600)
public class ShoppingCartController {
    
    @Autowired
    private ShoppingCartService shoppingCartService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * 获取购物车列表
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<?> getCart(Authentication authentication) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            List<ShoppingCart> cartItems = shoppingCartService.getCartItems(customer);
            return ResponseEntity.ok(cartItems);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 添加商品到购物车
     * POST /api/cart
     */
    @PostMapping
    public ResponseEntity<?> addToCart(Authentication authentication, @RequestBody Map<String, Object> request) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
            ShoppingCart cart = shoppingCartService.addToCart(customer, productId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已添加到购物车");
            response.put("cart", cart);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新购物车商品数量
     * PUT /api/cart/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCart(Authentication authentication, @PathVariable Long id, 
                                       @RequestBody Map<String, Integer> request) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            Integer quantity = request.get("quantity");
            ShoppingCart cart = shoppingCartService.updateCartQuantity(customer, id, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "数量已更新");
            response.put("cart", cart);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从购物车移除商品
     * DELETE /api/cart/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(Authentication authentication, @PathVariable Long id) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            shoppingCartService.removeFromCart(customer, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已移除");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 将购物车商品转为收藏
     * POST /api/cart/move-to-favorites
     */
    @PostMapping("/move-to-favorites")
    public ResponseEntity<?> moveToFavorites(Authentication authentication, @RequestBody Map<String, Object> request) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            @SuppressWarnings("unchecked")
            List<Long> cartIds = (List<Long>) request.get("cartIds");
            
            shoppingCartService.moveToFavorites(customer, cartIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已转为收藏");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 清空购物车
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            shoppingCartService.clearCart(customer);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "购物车已清空");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
