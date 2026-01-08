package com.shop.controller;

import com.shop.entity.Customer;
import com.shop.entity.DeliveryAddress;
import com.shop.service.DeliveryAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class DeliveryAddressController {
    
    @Autowired
    private DeliveryAddressService deliveryAddressService;
    
    // 获取客户的所有收货地址
    @GetMapping
    public ResponseEntity<List<DeliveryAddress>> getAddresses(Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        List<DeliveryAddress> addresses = deliveryAddressService.getAddressesByCustomer(customer);
        return ResponseEntity.ok(addresses);
    }
    
    // 获取默认地址
    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress(Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        DeliveryAddress address = deliveryAddressService.getDefaultAddress(customer);
        if (address == null) {
            return ResponseEntity.ok(Collections.singletonMap("message", "未设置默认地址"));
        }
        return ResponseEntity.ok(address);
    }
    
    // 添加收货地址
    @PostMapping
    public ResponseEntity<DeliveryAddress> addAddress(@RequestBody DeliveryAddress address, 
                                                      Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        DeliveryAddress saved = deliveryAddressService.addAddress(customer, address);
        return ResponseEntity.ok(saved);
    }
    
    // 更新收货地址
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryAddress> updateAddress(@PathVariable Long id, 
                                                         @RequestBody DeliveryAddress address,
                                                         Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        DeliveryAddress updated = deliveryAddressService.updateAddress(id, customer, address);
        return ResponseEntity.ok(updated);
    }
    
    // 设置默认地址
    @PutMapping("/{id}/default")
    public ResponseEntity<Map<String, String>> setDefaultAddress(@PathVariable Long id, 
                                                                  Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        deliveryAddressService.setDefaultAddress(id, customer);
        Map<String, String> response = new HashMap<>();
        response.put("message", "默认地址设置成功");
        return ResponseEntity.ok(response);
    }
    
    // 删除收货地址
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAddress(@PathVariable Long id, 
                                                              Authentication authentication) {
        Customer customer = (Customer) authentication.getPrincipal();
        deliveryAddressService.deleteAddress(id, customer);
        Map<String, String> response = new HashMap<>();
        response.put("message", "地址删除成功");
        return ResponseEntity.ok(response);
    }
    
    // 获取地址详情
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryAddress> getAddress(@PathVariable Long id) {
        DeliveryAddress address = deliveryAddressService.getAddressById(id);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(address);
    }
}
