package com.shop.controller;

import com.shop.entity.Product;
import com.shop.entity.Seller;
import com.shop.exception.ProductNotFoundException;
import com.shop.service.ProductService;
import com.shop.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 修改第21行的@RequestMapping
@RestController
@RequestMapping("/products") // 移除/api前缀
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerService sellerService;

    // 获取所有可购买的商品（供买家查看）
    @GetMapping
    public ResponseEntity<List<Product>> getAllAvailableProducts() {
        List<Product> products = productService.getAllAvailableProducts();
        return ResponseEntity.ok(products);
    }

    // 获取单个商品详情
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 创建新商品（卖家操作）
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            Seller seller = sellerService.getSellerByUsername(username);
            product.setSeller(seller);
            Product savedProduct = productService.saveProduct(product);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 更新商品信息
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            // 验证商品所有权
            Product existingProduct = productService.getProductById(id);
            Seller seller = sellerService.getSellerByUsername(username);
            
            if (existingProduct.getSeller().getId().equals(seller.getId())) {
                product.setId(id);
                product.setSeller(seller);
                Product updatedProduct = productService.updateProduct(product);
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 上架商品
    @PutMapping("/{id}/publish")
    public ResponseEntity<Product> publishProduct(@PathVariable Long id) {
        return changeProductStatus(id, true);
    }

    // 下架商品
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Product> unpublishProduct(@PathVariable Long id) {
        return changeProductStatus(id, false);
    }

    // 辅助方法：更改商品状态
    private ResponseEntity<Product> changeProductStatus(Long id, boolean publish) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            // 验证商品所有权
            Product product = productService.getProductById(id);
            Seller seller = sellerService.getSellerByUsername(username);
            
            if (product.getSeller().getId().equals(seller.getId())) {
                Product updatedProduct = publish ? 
                        productService.publishProduct(id) : 
                        productService.unpublishProduct(id);
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 获取当前登录卖家的所有商品
    @GetMapping("/seller")
    public ResponseEntity<List<Product>> getSellerProducts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            Seller seller = sellerService.getSellerByUsername(username);
            List<Product> products = productService.getProductsBySellerId(seller.getId());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 删除商品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            // 验证商品所有权
            Product product = productService.getProductById(id);
            Seller seller = sellerService.getSellerByUsername(username);
            
            if (product.getSeller().getId().equals(seller.getId())) {
                productService.deleteProduct(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}