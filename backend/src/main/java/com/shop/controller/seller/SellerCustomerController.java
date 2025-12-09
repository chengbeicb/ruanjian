package com.shop.controller.seller;

import com.shop.entity.Customer;
import com.shop.entity.Order;
import com.shop.repository.CustomerRepository;
import com.shop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/customers")
public class SellerCustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 获取所有客户列表
     * GET /api/seller/customers
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(@RequestParam(required = false) String keyword) {
        List<Customer> customers;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索客户（按用户名或手机号）
            customers = customerRepository.findByUsernameContainingOrPhoneContaining(keyword.trim(), keyword.trim());
        } else {
            customers = customerRepository.findAll();
        }
        return ResponseEntity.ok(customers);
    }
    
    /**
     * 获取客户详情
     * GET /api/seller/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取客户的订单历史
     * GET /api/seller/customers/{id}/orders
     */
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable Long id) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreateTimeDesc(id);
        return ResponseEntity.ok(orders);
    }
}
