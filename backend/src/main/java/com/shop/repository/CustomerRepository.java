package com.shop.repository;

import com.shop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);
    
    // 按用户名或手机号搜索客户
    List<Customer> findByUsernameContainingOrPhoneContaining(String username, String phone);
}