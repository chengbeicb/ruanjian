package com.shop.repository;

import com.shop.entity.Customer;
import com.shop.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    
    // 查询客户的所有收货地址
    List<DeliveryAddress> findByCustomerOrderByIsDefaultDescCreateTimeDesc(Customer customer);
    
    // 查询客户的默认地址
    Optional<DeliveryAddress> findByCustomerAndIsDefaultTrue(Customer customer);
    
    // 查询客户的地址数量
    long countByCustomer(Customer customer);
}
