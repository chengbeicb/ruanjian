package com.shop.repository;

import com.shop.entity.AfterSale;
import com.shop.entity.AfterSaleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfterSaleImageRepository extends JpaRepository<AfterSaleImage, Long> {
    
    // 查询售后申请的所有图片
    List<AfterSaleImage> findByAfterSaleOrderByImageOrder(AfterSale afterSale);
}
