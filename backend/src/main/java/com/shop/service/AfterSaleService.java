package com.shop.service;

import com.shop.entity.*;
import com.shop.repository.AfterSaleImageRepository;
import com.shop.repository.AfterSaleRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AfterSaleService {
    
    @Autowired
    private AfterSaleRepository afterSaleRepository;
    
    @Autowired
    private AfterSaleImageRepository afterSaleImageRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    // 创建售后申请
    @Transactional
    public AfterSale createAfterSale(Customer customer, Long orderItemId, AfterSale.ServiceType serviceType, 
                                     String reason, String description, List<String> imageUrls) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("订单明细不存在"));
        
        Order order = orderItem.getOrder();
        
        // 验证订单是否属于该客户
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("无权对此订单申请售后");
        }
        
        // 验证订单状态（只有已完成的订单才能申请售后）
        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("只有已完成的订单才能申请售后");
        }
        
        AfterSale afterSale = new AfterSale();
        afterSale.setAfterSaleNumber("AS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        afterSale.setOrder(order);
        afterSale.setOrderItem(orderItem);
        afterSale.setCustomer(customer);
        afterSale.setProduct(orderItem.getProduct());
        afterSale.setServiceType(serviceType);
        afterSale.setReason(reason);
        afterSale.setDescription(description);
        afterSale.setStatus(AfterSale.AfterSaleStatus.PENDING);
        afterSale.setCreateTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());
        
        // 计算退款金额（订单明细的小计）
        if (serviceType == AfterSale.ServiceType.REFUND || serviceType == AfterSale.ServiceType.RETURN) {
            afterSale.setRefundAmount(orderItem.getSubtotal());
        }
        
        afterSale = afterSaleRepository.save(afterSale);
        
        // 保存图片
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                AfterSaleImage image = new AfterSaleImage();
                image.setAfterSale(afterSale);
                image.setImageUrl(imageUrls.get(i));
                image.setImageOrder(i);
                image.setCreateTime(LocalDateTime.now());
                afterSaleImageRepository.save(image);
            }
        }
        
        return afterSale;
    }
    
    // 客户获取自己的售后申请列表
    public List<AfterSale> getAfterSalesByCustomer(Customer customer) {
        return afterSaleRepository.findByCustomerOrderByCreateTimeDesc(customer);
    }
    
    // 商家获取所有售后申请列表
    public List<AfterSale> getAllAfterSales() {
        return afterSaleRepository.findAllByOrderByCreateTimeDesc();
    }
    
    // 获取售后申请详情
    public AfterSale getAfterSaleById(Long afterSaleId) {
        return afterSaleRepository.findById(afterSaleId)
                .orElseThrow(() -> new RuntimeException("售后申请不存在"));
    }
    
    // 商家处理售后申请
    @Transactional
    public AfterSale processAfterSale(Long afterSaleId, AfterSale.AfterSaleStatus status, String sellerReply) {
        AfterSale afterSale = getAfterSaleById(afterSaleId);
        
        if (afterSale.getStatus() != AfterSale.AfterSaleStatus.PENDING && 
            afterSale.getStatus() != AfterSale.AfterSaleStatus.PROCESSING) {
            throw new RuntimeException("售后申请状态不允许修改");
        }
        
        afterSale.setStatus(status);
        afterSale.setSellerReply(sellerReply);
        afterSale.setUpdateTime(LocalDateTime.now());
        
        return afterSaleRepository.save(afterSale);
    }
    
    // 完成售后申请
    @Transactional
    public AfterSale completeAfterSale(Long afterSaleId, AfterSale.AfterSaleResult result) {
        AfterSale afterSale = getAfterSaleById(afterSaleId);
        
        if (afterSale.getStatus() != AfterSale.AfterSaleStatus.APPROVED) {
            throw new RuntimeException("售后申请未同意，无法完成");
        }
        
        afterSale.setStatus(AfterSale.AfterSaleStatus.COMPLETED);
        afterSale.setResult(result);
        afterSale.setCompleteTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());
        
        // 如果是退款，更新订单支付状态
        if (result == AfterSale.AfterSaleResult.REFUNDED) {
            Order order = afterSale.getOrder();
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
            order.setUpdateTime(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        return afterSaleRepository.save(afterSale);
    }
    
    // 获取售后申请的图片列表
    public List<AfterSaleImage> getAfterSaleImages(Long afterSaleId) {
        AfterSale afterSale = getAfterSaleById(afterSaleId);
        return afterSaleImageRepository.findByAfterSaleOrderByImageOrder(afterSale);
    }
}
