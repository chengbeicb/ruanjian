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
    
    @Autowired
    private javax.persistence.EntityManager entityManager;
    
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
    public List<Product> searchProducts(String keyword, String categoryLevel1, String categoryLevel2) {
        // 构建查询条件
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE p.status = :status");
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            jpql.append(" AND (p.name LIKE :keyword OR p.description LIKE :keyword)");
        }
        
        // 添加一级分类搜索条件
        if (categoryLevel1 != null && !categoryLevel1.isEmpty()) {
            jpql.append(" AND p.categoryLevel1 = :categoryLevel1");
        }
        
        // 添加二级分类搜索条件
        if (categoryLevel2 != null && !categoryLevel2.isEmpty()) {
            jpql.append(" AND p.categoryLevel2 = :categoryLevel2");
        }
        
        // 创建查询
        javax.persistence.Query query = entityManager.createQuery(jpql.toString());
        query.setParameter("status", Product.ProductStatus.AVAILABLE);
        
        // 设置参数
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (categoryLevel1 != null && !categoryLevel1.isEmpty()) {
            query.setParameter("categoryLevel1", categoryLevel1);
        }
        if (categoryLevel2 != null && !categoryLevel2.isEmpty()) {
            query.setParameter("categoryLevel2", categoryLevel2);
        }
        
        return query.getResultList();
    }
    
    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("商品不存在，ID: " + id));
    }
    
    @Override
    @Transactional
    public Product saveProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        // 检查库存数量，如果为0则自动设置为下架状态
        if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
            product.setStatus(Product.ProductStatus.UNPUBLISHED);
        } else {
            product.setStatus(Product.ProductStatus.AVAILABLE);
        }
        
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public List<Product> batchSaveProducts(List<Product> products) {
        for (Product product : products) {
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            
            // 检查库存数量，如果为0则自动设置为下架状态
            if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
                product.setStatus(Product.ProductStatus.UNPUBLISHED);
            } else {
                product.setStatus(Product.ProductStatus.AVAILABLE);
            }
        }
        return productRepository.saveAll(products);
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
        Product existingProduct = getProductById(product.getId());
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setImageUrls(product.getImageUrls());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategoryLevel1(product.getCategoryLevel1());
        existingProduct.setCategoryLevel2(product.getCategoryLevel2());
        existingProduct.setUpdateTime(LocalDateTime.now());
        
        // 检查库存数量，如果为0则自动设置为下架状态
        if (existingProduct.getStockQuantity() == null || existingProduct.getStockQuantity() <= 0) {
            existingProduct.setStatus(Product.ProductStatus.UNPUBLISHED);
        } else if (existingProduct.getStatus() == Product.ProductStatus.UNPUBLISHED) {
            // 如果之前是下架状态，且现在库存大于0，则设置为可购买状态
            existingProduct.setStatus(Product.ProductStatus.AVAILABLE);
        }
        
        return productRepository.save(existingProduct);
    }
    
    @Override
    @Transactional
    public void updateStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        int newStock = product.getStockQuantity() - quantity;
        product.setStockQuantity(newStock);
        
        // 如果库存变为0，则自动下架
        if (newStock <= 0) {
            product.setStatus(Product.ProductStatus.UNPUBLISHED);
        }
        
        productRepository.save(product);
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