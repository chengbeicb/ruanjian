package com.shop.service;

import com.shop.entity.Product;
import com.shop.exception.ProductNotFoundException;
import com.shop.exception.ProductStatusException;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    // 额外的方法，用于创建新商品
    @Transactional
    public Product createProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus(Product.ProductStatus.AVAILABLE);
        return productRepository.save(product);
    }
    
    @Override
    public List<Product> getAllAvailableProducts() {
        return productRepository.findByStatus(Product.ProductStatus.AVAILABLE);
    }
    
    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("商品不存在，ID: " + id));
    }
    
    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Override
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
    
    @Override
    @Transactional
    public Product publishProduct(Long id) {
        Product product = getProductById(id);
        product.setStatus(Product.ProductStatus.AVAILABLE);
        product.setUpdateTime(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product unpublishProduct(Long id) {
        Product product = getProductById(id);
        // 修改为使用新的UNPUBLISHED状态，而不是SOLD
        product.setStatus(Product.ProductStatus.UNPUBLISHED);
        product.setUpdateTime(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product updateProduct(Product product) {
        // 检查商品是否存在
        Product existingProduct = getProductById(product.getId());
        
        // 更新商品信息
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setUpdateTime(LocalDateTime.now());
        
        return productRepository.save(existingProduct);
    }
    
    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
    
    @Override
    @Transactional
    public void freezeProduct(Long id) {
        Product product = getProductById(id);
        
        // 检查商品是否可被冻结（必须是可购买状态）
        if (product.getStatus() != Product.ProductStatus.AVAILABLE) {
            throw new ProductStatusException("商品状态不允许冻结");
        }
        
        product.setStatus(Product.ProductStatus.FROZEN);
        product.setUpdateTime(LocalDateTime.now());
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void unfreezeProduct(Long id) {
        Product product = getProductById(id);
        
        // 检查商品是否可被解冻（必须是冻结状态）
        if (product.getStatus() != Product.ProductStatus.FROZEN) {
            throw new ProductStatusException("商品状态不允许解冻");
        }
        
        product.setStatus(Product.ProductStatus.AVAILABLE);
        product.setUpdateTime(LocalDateTime.now());
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void markProductAsSold(Long id) {
        Product product = getProductById(id);
        product.setStatus(Product.ProductStatus.SOLD);
        product.setUpdateTime(LocalDateTime.now());
        productRepository.save(product);
    }
}