package com.shop.service;

import com.shop.entity.Customer;
import com.shop.exception.AuthenticationException;
import com.shop.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Customer registerCustomer(Customer customer) {
        // 检查用户名是否已存在
        if (existsByUsername(customer.getUsername())) {
            throw new AuthenticationException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (existsByPhone(customer.getPhone())) {
            throw new AuthenticationException("手机号已被注册");
        }
        
        // 加密密码
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setActive(true);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());
        
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AuthenticationException("客户不存在，ID: " + id));
    }

    @Override
    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("客户不存在，用户名: " + username));
    }

    @Override
    public boolean authenticate(String username, String password) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));
        
        // 如果账号未激活，提供更明确的错误信息
        if (!customer.isActive()) {
            throw new AuthenticationException("账号未激活，请联系管理员");
        }
        
        return passwordEncoder.matches(password, customer.getPassword());
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        return customerRepository.existsByPhone(phone);
    }
    
    @Override
    @Transactional
    public Customer updateCustomer(Customer customer) {
        // 检查客户是否存在
        Customer existingCustomer = getCustomerById(customer.getId());
        
        // 更新客户信息，但不更新密码
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setDefaultAddress(customer.getDefaultAddress());
        existingCustomer.setUpdateTime(LocalDateTime.now());
        
        return customerRepository.save(existingCustomer);
    }
    
    @Override
    @Transactional
    public boolean updatePassword(Long customerId, String oldPassword, String newPassword) {
        Customer customer = getCustomerById(customerId);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new AuthenticationException("当前密码不正确");
        }
        
        // 加密新密码并更新
        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setUpdateTime(LocalDateTime.now());
        customerRepository.save(customer);
        
        return true;
    }
    
    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}