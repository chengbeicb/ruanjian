package com.shop.service;

import com.shop.entity.Favorite;
import com.shop.entity.Customer;
import com.shop.entity.Product;
import com.shop.repository.FavoriteRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 收藏商品
     */
    @Transactional
    public Favorite addFavorite(Customer customer, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查是否已收藏
        if (favoriteRepository.existsByCustomerAndProduct(customer, product)) {
            throw new RuntimeException("已收藏该商品");
        }
        
        Favorite favorite = new Favorite();
        favorite.setCustomer(customer);
        favorite.setProduct(product);
        
        return favoriteRepository.save(favorite);
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Customer customer, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        Favorite favorite = favoriteRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new RuntimeException("未收藏该商品"));
        
        favoriteRepository.delete(favorite);
    }
    
    /**
     * 获取收藏列表
     */
    public List<Favorite> getFavorites(Customer customer) {
        return favoriteRepository.findByCustomerOrderByCreateTimeDesc(customer);
    }
    
    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Customer customer, Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }
        return favoriteRepository.existsByCustomerAndProduct(customer, product);
    }
}
