package com.shop.repository;

import com.shop.entity.Order;
import com.shop.entity.Order.OrderStatus;
import com.shop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 根据订单编号查找
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // 查找客户的所有订单
    List<Order> findByCustomerOrderByCreateTimeDesc(Customer customer);
    
    // 查找指定状态的订单
    List<Order> findByStatusOrderByCreateTimeDesc(OrderStatus status);
    
    // 查找客户的指定状态订单
    List<Order> findByCustomerAndStatusOrderByCreateTimeDesc(Customer customer, OrderStatus status);
    
    // 获取所有订单（按创建时间倒序）
    List<Order> findAllByOrderByCreateTimeDesc();
    
    // 根据客户ID查找订单
    List<Order> findByCustomerIdOrderByCreateTimeDesc(Long customerId);
}
