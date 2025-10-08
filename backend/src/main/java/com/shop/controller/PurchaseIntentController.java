package com.shop.controller;

import com.shop.entity.Product;
import com.shop.entity.PurchaseIntent;
import com.shop.entity.Seller;
import com.shop.exception.ProductNotFoundException;
import com.shop.exception.ProductStatusException;
import com.shop.exception.PurchaseIntentNotFoundException;
import com.shop.exception.SellerNotFoundException;
import com.shop.service.ProductService;
import com.shop.service.PurchaseIntentService;
import com.shop.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 假设第22行左右有@RequestMapping
@RestController
@RequestMapping("/purchase-intents") // 移除/api前缀
@CrossOrigin(origins = "*", maxAge = 3600)
public class PurchaseIntentController {

    @Autowired
    private PurchaseIntentService purchaseIntentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerService sellerService;

    // 创建购买意向（买家操作）
    @PostMapping
    public ResponseEntity<?> createPurchaseIntent(@RequestBody PurchaseIntent purchaseIntent) {
        try {
            Long productId = purchaseIntent.getProduct().getId();
            
            // 验证商品状态
            Product product = productService.getProductById(productId);
            
            // 检查是否已有该商品的活跃购买意向
            if (purchaseIntentService.getActivePurchaseIntentByProductId(productId) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("该商品已有买家表达购买意向");
            }
            
            purchaseIntent.setProduct(product);
            PurchaseIntent savedIntent = purchaseIntentService.createPurchaseIntent(purchaseIntent);
            return new ResponseEntity<>(savedIntent, HttpStatus.CREATED);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ProductStatusException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建购买意向失败");
        }
    }

    // 卖家获取所有购买意向
    @GetMapping
    public ResponseEntity<List<PurchaseIntent>> getPurchaseIntents() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Seller seller = sellerService.getSellerByUsername(username);
            List<PurchaseIntent> intents = purchaseIntentService.getPurchaseIntentsBySellerId(seller.getId());
            return ResponseEntity.ok(intents);
        } catch (SellerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 卖家完成交易
    @PutMapping("/{id}/complete")
    public ResponseEntity<PurchaseIntent> completePurchase(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Seller seller = sellerService.getSellerByUsername(username);
            
            // 验证购买意向所有权
            PurchaseIntent intent = purchaseIntentService.getPurchaseIntentById(id);
            if (!intent.getProduct().getSeller().getId().equals(seller.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            PurchaseIntent completedIntent = purchaseIntentService.completePurchaseIntent(id);
            return ResponseEntity.ok(completedIntent);
        } catch (PurchaseIntentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SellerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 卖家取消交易
    @PutMapping("/{id}/cancel")
    public ResponseEntity<PurchaseIntent> cancelPurchase(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Seller seller = sellerService.getSellerByUsername(username);
            
            // 验证购买意向所有权
            PurchaseIntent intent = purchaseIntentService.getPurchaseIntentById(id);
            if (!intent.getProduct().getSeller().getId().equals(seller.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            PurchaseIntent canceledIntent = purchaseIntentService.cancelPurchaseIntent(id);
            return ResponseEntity.ok(canceledIntent);
        } catch (PurchaseIntentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SellerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 获取单个购买意向详情
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseIntent> getPurchaseIntentById(@PathVariable Long id) {
        try {
            PurchaseIntent intent = purchaseIntentService.getPurchaseIntentById(id);
            
            // 验证访问权限（买家或卖家）
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                
                // 检查是否是卖家或者购买者
                try {
                    Seller seller = sellerService.getSellerByUsername(username);
                    if (intent.getProduct().getSeller().getId().equals(seller.getId())) {
                        return ResponseEntity.ok(intent);
                    }
                } catch (SellerNotFoundException e) {
                    // 如果不是卖家，则检查是否是购买者（此处需要根据系统设计调整）
                    // 假设购买者信息存储在PurchaseIntent中
                    if (intent.getBuyerEmail() != null && intent.getBuyerEmail().equals(username)) {
                        return ResponseEntity.ok(intent);
                    }
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (PurchaseIntentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 根据商品ID获取购买意向（卖家专用）
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PurchaseIntent>> getPurchaseIntentsByProductId(@PathVariable Long productId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Seller seller = sellerService.getSellerByUsername(username);
            
            // 验证商品所有权
            Product product = productService.getProductById(productId);
            if (!product.getSeller().getId().equals(seller.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 使用现有方法获取卖家的所有购买意向，然后过滤出特定商品的
            List<PurchaseIntent> allIntents = purchaseIntentService.getPurchaseIntentsBySellerId(seller.getId());
            List<PurchaseIntent> productIntents = allIntents.stream()
                    .filter(intent -> intent.getProduct().getId().equals(productId))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(productIntents);
        } catch (ProductNotFoundException | SellerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}