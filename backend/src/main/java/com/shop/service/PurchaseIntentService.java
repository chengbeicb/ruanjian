package com.shop.service;

import com.shop.entity.PurchaseIntent;

import java.util.List;

public interface PurchaseIntentService {
    PurchaseIntent createPurchaseIntent(PurchaseIntent purchaseIntent);
    List<PurchaseIntent> getPurchaseIntentsBySellerId(Long sellerId);
    PurchaseIntent getPurchaseIntentById(Long id);
    PurchaseIntent getActivePurchaseIntentByProductId(Long productId);
    PurchaseIntent completePurchaseIntent(Long id);
    PurchaseIntent cancelPurchaseIntent(Long id);
    List<PurchaseIntent> getPurchaseIntentsByCustomerId(Long customerId);
}