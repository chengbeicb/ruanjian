package com.shop.service;

import com.shop.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllAvailableProducts();
    Product getProductById(Long id);
    Product saveProduct(Product product);
    void deleteProduct(Long id);
    Product publishProduct(Long id);
    Product unpublishProduct(Long id);
    Product updateProduct(Product product);
    List<Product> getProductsBySellerId(Long sellerId);
    void freezeProduct(Long id);
    void unfreezeProduct(Long id);
    void markProductAsSold(Long id);
}