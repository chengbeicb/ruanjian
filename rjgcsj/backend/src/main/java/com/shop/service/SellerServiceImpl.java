package com.shop.service;

import com.shop.entity.Seller;
import com.shop.exception.AuthenticationException;
import com.shop.exception.SellerNotFoundException;
import com.shop.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Seller registerSeller(Seller seller) {
        // 检查用户名是否已存在
        if (existsByUsername(seller.getUsername())) {
            throw new AuthenticationException("用户名已存在");
        }
        
        // 加密密码
        seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        seller.setActive(true);
        seller.setCreateTime(LocalDateTime.now());
        seller.setUpdateTime(LocalDateTime.now());
        
        return sellerRepository.save(seller);
    }

    @Override
    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException("卖家不存在，ID: " + id));
    }

    @Override
    public Seller getSellerByUsername(String username) {
        return sellerRepository.findByUsername(username)
                .orElseThrow(() -> new SellerNotFoundException("卖家不存在，用户名: " + username));
    }

    @Override
    @Transactional
    public Seller updateSeller(Seller seller) {
        // 检查卖家是否存在
        Seller existingSeller = getSellerById(seller.getId());
        
        // 更新卖家信息，但不更新密码
        existingSeller.setEmail(seller.getEmail());
        existingSeller.setStoreName(seller.getStoreName());
        existingSeller.setContactPhone(seller.getContactPhone());
        existingSeller.setUpdateTime(LocalDateTime.now());
        
        return sellerRepository.save(existingSeller);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long sellerId, String oldPassword, String newPassword) {
        Seller seller = getSellerById(sellerId);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, seller.getPassword())) {
            throw new AuthenticationException("当前密码不正确");
        }
        
        // 加密新密码并更新
        seller.setPassword(passwordEncoder.encode(newPassword));
        seller.setUpdateTime(LocalDateTime.now());
        sellerRepository.save(seller);
        
        return true;
    }

    @Override
    public boolean authenticate(String username, String password) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));
        
        // 如果账号未激活，提供更明确的错误信息
        if (!Boolean.TRUE.equals(seller.getActive())) {
            throw new AuthenticationException("账号未激活，请联系管理员");
        }
        
        return passwordEncoder.matches(password, seller.getPassword());
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return sellerRepository.existsByUsername(username);
    }
}