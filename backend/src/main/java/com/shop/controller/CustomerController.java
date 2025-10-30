package com.shop.controller;

import com.shop.entity.Customer;
import com.shop.entity.PurchaseIntent;
import com.shop.exception.AuthenticationException;
import com.shop.service.CustomerService;
import com.shop.service.PurchaseIntentService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private PurchaseIntentService purchaseIntentService;

    // 客户注册
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        try {
            Customer registeredCustomer = customerService.registerCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredCustomer);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败");
        }
    }

    // 客户登录验证
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            // 验证凭证
            customerService.authenticate(username, password);
            
            // 创建认证对象并设置到安全上下文
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("authenticated", true);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 获取当前登录客户信息
    @GetMapping("/me")
    public ResponseEntity<Customer> getCurrentCustomer() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Customer customer = customerService.getCustomerByUsername(username);
            return ResponseEntity.ok(customer);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 更新客户信息
    @PutMapping
    public ResponseEntity<?> updateCustomer(@RequestBody Customer updatedCustomer) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // 验证客户身份
            Customer currentCustomer = customerService.getCustomerByUsername(username);
            updatedCustomer.setId(currentCustomer.getId());
            updatedCustomer.setUsername(currentCustomer.getUsername()); // 不允许修改用户名
            
            Customer savedCustomer = customerService.updateCustomer(updatedCustomer);
            return ResponseEntity.ok(savedCustomer);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新客户信息失败");
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
            
            // 获取当前客户
            Customer customer = customerService.getCustomerByUsername(username);
            
            // 更新密码
            customerService.updatePassword(customer.getId(), oldPassword, newPassword);
            
            return ResponseEntity.ok("密码更新成功");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("密码更新失败");
        }
    }

    // 获取当前客户的购买历史
    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseIntent>> getCustomerOrders() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Customer customer = customerService.getCustomerByUsername(username);
            List<PurchaseIntent> orders = purchaseIntentService.getPurchaseIntentsByCustomerId(customer.getId());
            
            return ResponseEntity.ok(orders);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 管理员获取所有客户信息
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        try {
            // 这里可以添加权限验证，确保只有管理员可以访问
            List<Customer> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}