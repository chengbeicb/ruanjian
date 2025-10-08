package com.shop.service;

import com.shop.entity.PurchaseIntent;
import com.shop.entity.Product;
import com.shop.exception.ProductStatusException;
import com.shop.exception.PurchaseIntentNotFoundException;
import com.shop.repository.PurchaseIntentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseIntentServiceImpl implements PurchaseIntentService {

    @Autowired
    private PurchaseIntentRepository purchaseIntentRepository;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public PurchaseIntent createPurchaseIntent(PurchaseIntent purchaseIntent) {
        // 检查购买意向是否已存在
        if (getActivePurchaseIntentByProductId(purchaseIntent.getProduct().getId()) != null) {
            throw new ProductStatusException("该商品已有购买意向正在处理中");
        }
        
        // 冻结商品
        productService.freezeProduct(purchaseIntent.getProduct().getId());
        
        // 设置购买意向状态和时间
        purchaseIntent.setCompleted(false);
        purchaseIntent.setCanceled(false);
        purchaseIntent.setCreateTime(LocalDateTime.now());
        
        return purchaseIntentRepository.save(purchaseIntent);
    }

    @Override
    public List<PurchaseIntent> getPurchaseIntentsBySellerId(Long sellerId) {
        return purchaseIntentRepository.findByProductSellerId(sellerId);
    }

    @Override
    public PurchaseIntent getPurchaseIntentById(Long id) {
        return purchaseIntentRepository.findById(id)
                .orElseThrow(() -> new PurchaseIntentNotFoundException("购买意向不存在，ID: " + id));
    }

    @Override
    public PurchaseIntent getActivePurchaseIntentByProductId(Long productId) {
        return purchaseIntentRepository.findByProductIdAndCompletedFalseAndCanceledFalse(productId)
                .orElse(null);
    }

    @Override
    @Transactional
    public PurchaseIntent completePurchaseIntent(Long id) {
        PurchaseIntent purchaseIntent = getPurchaseIntentById(id);
        
        // 检查购买意向状态
        if (purchaseIntent.getCompleted() || purchaseIntent.getCanceled()) {
            throw new ProductStatusException("购买意向状态不允许完成");
        }
        
        // 标记购买意向为已完成
        purchaseIntent.setCompleted(true);
        purchaseIntent.setCompleteTime(LocalDateTime.now());
        
        // 商品下架（标记为已售出）
        productService.markProductAsSold(purchaseIntent.getProduct().getId());
        
        return purchaseIntentRepository.save(purchaseIntent);
    }

    @Override
    @Transactional
    public PurchaseIntent cancelPurchaseIntent(Long id) {
        PurchaseIntent purchaseIntent = getPurchaseIntentById(id);
        
        // 检查购买意向状态
        if (purchaseIntent.getCompleted() || purchaseIntent.getCanceled()) {
            throw new ProductStatusException("购买意向状态不允许取消");
        }
        
        // 标记购买意向为已取消
        purchaseIntent.setCanceled(true);
        
        // 解冻商品
        productService.unfreezeProduct(purchaseIntent.getProduct().getId());
        
        return purchaseIntentRepository.save(purchaseIntent);
    }
}