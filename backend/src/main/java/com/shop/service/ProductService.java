package com.shop.service;

import com.shop.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllAvailableProducts();
    Product getProductById(Long id);
    Product saveProduct(Product product);
    List<Product> batchSaveProducts(List<Product> products);
    void deleteProduct(Long id);
    Product publishProduct(Long id);
    Product unpublishProduct(Long id);
    Product updateProduct(Product product);
    void updateStock(Long productId, int quantity);
    List<Product> getProductsBySellerId(Long sellerId);
    List<Product> searchProducts(String keyword, String categoryLevel1, String categoryLevel2);
    void freezeProduct(Long id);
    void unfreezeProduct(Long id);
    void markProductAsSold(Long id);
}