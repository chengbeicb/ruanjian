package com.shop.service;

import com.shop.entity.ShoppingCart;
import com.shop.entity.Favorite;
import com.shop.entity.Customer;
import com.shop.entity.Product;
import com.shop.repository.ShoppingCartRepository;
import com.shop.repository.FavoriteRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {
    
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    /**
     * 添加商品到购物车
     */
    @Transactional
    public ShoppingCart addToCart(Customer customer, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查商品状态
        if (product.getStatus() != Product.ProductStatus.AVAILABLE) {
            throw new RuntimeException("商品不可购买");
        }
        
        // 检查库存
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        // 检查是否已在购物车
        Optional<ShoppingCart> existingCart = shoppingCartRepository.findByCustomerAndProduct(customer, product);
        
        if (existingCart.isPresent()) {
            // 更新数量
            ShoppingCart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            cart.setUpdateTime(LocalDateTime.now());
            return shoppingCartRepository.save(cart);
        } else {
            // 新增购物车记录
            ShoppingCart cart = new ShoppingCart();
            cart.setCustomer(customer);
            cart.setProduct(product);
            cart.setQuantity(quantity);
            return shoppingCartRepository.save(cart);
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @Transactional
    public ShoppingCart updateCartQuantity(Customer customer, Long cartId, Integer quantity) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("购物车记录不存在"));
        
        if (!cart.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("无权操作此购物车");
        }
        
        if (quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        
        // 检查库存
        if (cart.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        return shoppingCartRepository.save(cart);
    }
    
    /**
     * 从购物车移除商品
     */
    @Transactional
    public void removeFromCart(Customer customer, Long cartId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("购物车记录不存在"));
        
        if (!cart.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("无权操作此购物车");
        }
        
        shoppingCartRepository.delete(cart);
    }
    
    /**
     * 获取购物车列表
     */
    public List<ShoppingCart> getCartItems(Customer customer) {
        return shoppingCartRepository.findByCustomerOrderByCreateTimeDesc(customer);
    }
    
    /**
     * 将购物车商品转为收藏
     */
    @Transactional
    public void moveToFavorites(Customer customer, List<Long> cartIds) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByIdIn(cartIds);
        
        for (ShoppingCart cart : cartItems) {
            if (!cart.getCustomer().getId().equals(customer.getId())) {
                continue;
            }
            
            // 检查是否已收藏
            if (!favoriteRepository.existsByCustomerAndProduct(customer, cart.getProduct())) {
                Favorite favorite = new Favorite();
                favorite.setCustomer(customer);
                favorite.setProduct(cart.getProduct());
                favoriteRepository.save(favorite);
            }
            
            // 删除购物车记录
            shoppingCartRepository.delete(cart);
        }
    }
    
    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart(Customer customer) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByCustomerOrderByCreateTimeDesc(customer);
        shoppingCartRepository.deleteAll(cartItems);
    }
}
