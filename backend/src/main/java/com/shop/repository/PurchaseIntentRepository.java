package com.shop.repository;

import com.shop.entity.PurchaseIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseIntentRepository extends JpaRepository<PurchaseIntent, Long> {
    List<PurchaseIntent> findByProductSellerId(Long sellerId);
    List<PurchaseIntent> findByProductId(Long productId);
    List<PurchaseIntent> findByCustomerId(Long customerId);
    Optional<PurchaseIntent> findByProductIdAndCompletedFalseAndCanceledFalse(Long productId);
}