package com.shop.controller;

import com.shop.entity.Favorite;
import com.shop.entity.Customer;
import com.shop.repository.CustomerRepository;
import com.shop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", maxAge = 3600)
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * 获取收藏列表
     * GET /api/favorites
     */
    @GetMapping
    public ResponseEntity<?> getFavorites(Authentication authentication) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            List<Favorite> favorites = favoriteService.getFavorites(customer);
            return ResponseEntity.ok(favorites);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 收藏商品
     * POST /api/favorites
     */
    @PostMapping
    public ResponseEntity<?> addFavorite(Authentication authentication, @RequestBody Map<String, Long> request) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            Long productId = request.get("productId");
            Favorite favorite = favoriteService.addFavorite(customer, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "收藏成功");
            response.put("favorite", favorite);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消收藏
     * DELETE /api/favorites/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFavorite(Authentication authentication, @PathVariable Long productId) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            favoriteService.removeFavorite(customer, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已取消收藏");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 检查是否已收藏
     * GET /api/favorites/check/{productId}
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<?> checkFavorite(Authentication authentication, @PathVariable Long productId) {
        try {
            Customer customer = customerRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("客户不存在"));
            
            boolean isFavorite = favoriteService.isFavorite(customer, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isFavorite", isFavorite);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
