package com.shop.service;

import com.shop.entity.Seller;

public interface SellerService {
    
    // 注册卖家账户
    Seller registerSeller(Seller seller);
    
    // 根据ID获取卖家信息
    Seller getSellerById(Long id);
    
    // 根据用户名获取卖家信息
    Seller getSellerByUsername(String username);
    
    // 更新卖家信息
    Seller updateSeller(Seller seller);
    
    // 修改卖家密码
    boolean updatePassword(Long sellerId, String oldPassword, String newPassword);
    
    // 验证卖家登录
    boolean authenticate(String username, String password);
    
    // 检查用户名是否已存在
    boolean existsByUsername(String username);
}