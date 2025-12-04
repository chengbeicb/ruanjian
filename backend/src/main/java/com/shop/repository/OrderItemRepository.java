package com.shop.repository;

import com.shop.entity.OrderItem;
import com.shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // 查找订单的所有商品明细
    List<OrderItem> findByOrder(Order order);
}
