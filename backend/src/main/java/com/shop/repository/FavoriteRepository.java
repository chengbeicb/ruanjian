package com.shop.repository;

import com.shop.entity.Favorite;
import com.shop.entity.Customer;
import com.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // 查找客户的所有收藏
    List<Favorite> findByCustomerOrderByCreateTimeDesc(Customer customer);
    
    // 查找客户是否收藏了某商品
    Optional<Favorite> findByCustomerAndProduct(Customer customer, Product product);
    
    // 检查客户是否收藏了某商品
    boolean existsByCustomerAndProduct(Customer customer, Product product);
}
