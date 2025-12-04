package com.shop.service;

import com.shop.entity.*;
import com.shop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    
    /**
     * 创建订单（从购物车）
     */
    @Transactional
    public Order createOrderFromCart(Customer customer, List<Long> cartIds, String receiverName, 
                                     String receiverPhone, String shippingAddress, String remark) {
        // 查找购物车商品
        List<ShoppingCart> cartItems = shoppingCartRepository.findByIdIn(cartIds);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNumber("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setCustomer(customer);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        order.setRemark(remark);
        
        double totalAmount = 0.0;
        
        // 创建订单商品明细
        for (ShoppingCart cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // 检查商品状态和库存
            if (product.getStatus() != Product.ProductStatus.AVAILABLE) {
                throw new RuntimeException("商品【" + product.getName() + "】不可购买");
            }
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("商品【" + product.getName() + "】库存不足");
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice() * cartItem.getQuantity());
            
            order.getOrderItems().add(orderItem);
            totalAmount += orderItem.getSubtotal();
            
            // 扣减库存
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        // 删除购物车商品
        shoppingCartRepository.deleteAll(cartItems);
        
        return savedOrder;
    }
    
    /**
     * 查询客户所有订单
     */
    public List<Order> getCustomerOrders(Customer customer) {
        return orderRepository.findByCustomerOrderByCreateTimeDesc(customer);
    }
    
    /**
     * 根据客户ID查询所有订单
     */
    public List<Order> getOrdersByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
        return getCustomerOrders(customer);
    }
    
    /**
     * 查询订单详情
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }
    
    /**
     * 商家确认订单
     */
    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("只有待确认订单才能确认");
        }
        
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    /**
     * 备货完成
     */
    @Transactional
    public Order prepareOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("只有已确认订单才能备货");
        }
        
        order.setStatus(Order.OrderStatus.PREPARING);
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    /**
     * 开始发货
     */
    @Transactional
    public Order shipOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.PREPARING) {
            throw new RuntimeException("只有备货完成订单才能发货");
        }
        
        order.setStatus(Order.OrderStatus.SHIPPING);
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    /**
     * 交易完成
     */
    @Transactional
    public Order completeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.SHIPPING) {
            throw new RuntimeException("只有发货中订单才能完成");
        }
        
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    /**
     * 客户取消订单（开始发货前可取消）
     */
    @Transactional
    public Order cancelOrderByCustomer(Long orderId, Long customerId, String reason) {
        Order order = getOrderById(orderId);
        
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("无权取消此订单");
        }
        
        if (order.getStatus() == Order.OrderStatus.SHIPPING || 
            order.getStatus() == Order.OrderStatus.COMPLETED ||
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已发货或已完成，无法取消");
        }
        
        return cancelOrder(order, Order.CancelRole.CUSTOMER, reason);
    }
    
    /**
     * 商家取消订单（交易完成前可取消）
     */
    @Transactional
    public Order cancelOrderBySeller(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() == Order.OrderStatus.COMPLETED ||
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已完成或已取消，无法再次取消");
        }
        
        return cancelOrder(order, Order.CancelRole.SELLER, reason);
    }
    
    /**
     * 取消订单（内部方法）
     */
    private Order cancelOrder(Order order, Order.CancelRole cancelRole, String reason) {
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelRole(cancelRole);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 恢复库存
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        
        return orderRepository.save(order);
    }
}
