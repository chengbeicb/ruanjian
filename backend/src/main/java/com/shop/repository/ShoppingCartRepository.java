package com.shop.repository;

import com.shop.entity.ShoppingCart;
import com.shop.entity.Customer;
import com.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    
    // 查找客户的购物车所有商品
    List<ShoppingCart> findByCustomerOrderByCreateTimeDesc(Customer customer);
    
    // 查找客户购物车中的某个商品
    Optional<ShoppingCart> findByCustomerAndProduct(Customer customer, Product product);
    
    // 检查购物车中是否有某商品
    boolean existsByCustomerAndProduct(Customer customer, Product product);
    
    // 批量查找购物车记录
    List<ShoppingCart> findByIdIn(List<Long> ids);
}
