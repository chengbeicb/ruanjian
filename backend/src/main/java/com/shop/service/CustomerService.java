package com.shop.service;

import com.shop.entity.Customer;
import com.shop.exception.AuthenticationException;

import java.util.List;

public interface CustomerService {
    
    // 注册新客户
    Customer registerCustomer(Customer customer);
    
    // 获取客户信息
    Customer getCustomerById(Long id);
    
    Customer getCustomerByUsername(String username);
    
    // 验证客户登录
    boolean authenticate(String username, String password);
    
    // 检查用户名是否存在
    boolean existsByUsername(String username);
    
    // 检查手机号是否存在
    boolean existsByPhone(String phone);
    
    // 更新客户信息
    Customer updateCustomer(Customer customer);
    
    // 更新客户密码
    boolean updatePassword(Long customerId, String oldPassword, String newPassword);
    
    // 获取所有客户信息（供管理员使用）
    List<Customer> getAllCustomers();
}