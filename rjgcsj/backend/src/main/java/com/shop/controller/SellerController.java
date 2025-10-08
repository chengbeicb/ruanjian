package com.shop.controller;

import com.shop.entity.Seller;
import com.shop.exception.AuthenticationException;
import com.shop.exception.SellerNotFoundException;
import com.shop.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 修改第21行的@RequestMapping
@RestController
@RequestMapping("/seller") // 移除/api前缀
@CrossOrigin(origins = "*", maxAge = 3600)
public class SellerController {

    @Autowired
    private SellerService sellerService;

    // 卖家登录验证
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            // 验证凭证
            sellerService.authenticate(username, password);
            
            // 创建认证对象并设置到安全上下文
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_SELLER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("authenticated", true);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 修改密码
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> passwordData) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            // 获取当前卖家
            Seller seller = sellerService.getSellerByUsername(username);
            
            // 更新密码
            sellerService.updatePassword(seller.getId(), oldPassword, newPassword);
            
            return ResponseEntity.ok("Password updated successfully");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update password");
        }
    }

    // 获取当前登录卖家信息
    @GetMapping("/me")
    public ResponseEntity<Seller> getCurrentSeller() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Seller seller = sellerService.getSellerByUsername(username);
            return ResponseEntity.ok(seller);
        } catch (SellerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 注册新卖家
    @PostMapping("/register")
    public ResponseEntity<?> registerSeller(@RequestBody Seller seller) {
        try {
            // 检查用户名是否已存在
            if (sellerService.existsByUsername(seller.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
            }
            
            Seller registeredSeller = sellerService.registerSeller(seller);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredSeller);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register seller");
        }
    }

    // 更新卖家信息
    @PutMapping
    public ResponseEntity<?> updateSeller(@RequestBody Seller updatedSeller) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // 验证卖家身份
            Seller currentSeller = sellerService.getSellerByUsername(username);
            updatedSeller.setId(currentSeller.getId());
            
            Seller savedSeller = sellerService.updateSeller(updatedSeller);
            return ResponseEntity.ok(savedSeller);
        } catch (SellerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update seller information");
        }
    }

    // 初始化卖家账号（系统启动时使用）
    @PostMapping("/init")
    public ResponseEntity<Seller> initSeller() {
        try {
            // 检查是否已有卖家账号
            if (sellerService.existsByUsername("admin")) {
                // 如果admin账号存在但未激活，自动激活它
                try {
                    Seller existingAdmin = sellerService.getSellerByUsername("admin");
                    if (!Boolean.TRUE.equals(existingAdmin.getActive())) {
                        existingAdmin.setActive(true);
                        sellerService.updateSeller(existingAdmin);
                        return ResponseEntity.ok(existingAdmin);
                    }
                } catch (SellerNotFoundException ignored) {
                    // 如果抛出异常，继续执行原逻辑
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            Seller seller = new Seller();
            seller.setUsername("admin");
            seller.setPassword("admin123"); // 初始密码
            seller.setActive(true); // 设置为活跃状态
            
            return ResponseEntity.ok(sellerService.registerSeller(seller));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}