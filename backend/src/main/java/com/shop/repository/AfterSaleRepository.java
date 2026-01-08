package com.shop.repository;

import com.shop.entity.AfterSale;
import com.shop.entity.Customer;
import com.shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfterSaleRepository extends JpaRepository<AfterSale, Long> {
    
    // 根据客户查询售后申请列表
    List<AfterSale> findByCustomerOrderByCreateTimeDesc(Customer customer);
    
    // 根据订单查询售后申请列表
    List<AfterSale> findByOrderOrderByCreateTimeDesc(Order order);
    
    // 查询所有售后申请（商家端）
    List<AfterSale> findAllByOrderByCreateTimeDesc();
    
    // 根据售后单号查询
    Optional<AfterSale> findByAfterSaleNumber(String afterSaleNumber);
}
