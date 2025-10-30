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
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryLevel1,
            @RequestParam(required = false) String categoryLevel2) {
        
        // 如果提供了搜索参数，则使用搜索功能
        if (keyword != null || categoryLevel1 != null || categoryLevel2 != null) {
            // 使用搜索功能查找商品
            List<Product> searchResults = productService.searchProducts(keyword, categoryLevel1, categoryLevel2);
            return ResponseEntity.ok(searchResults);
        }
        
        // 否则返回所有可用商品
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
            
            // 如果未设置状态，默认为已发布
            if (product.getStatus() == null) {
                product.setStatus(Product.ProductStatus.AVAILABLE);
            }
            
            Product savedProduct = productService.saveProduct(product);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<Product>> createProducts(@RequestBody List<Product> products) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            Seller seller = sellerService.getSellerByUsername(username);
            
            // 设置所有商品的卖家信息和默认状态
            for (Product product : products) {
                product.setSeller(seller);
                if (product.getStatus() == null) {
                    product.setStatus(Product.ProductStatus.AVAILABLE);
                }
            }
            
            List<Product> savedProducts = productService.batchSaveProducts(products);
            return new ResponseEntity<>(savedProducts, HttpStatus.CREATED);
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