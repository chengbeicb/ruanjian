package com.shop.service;

import com.shop.entity.Customer;
import com.shop.entity.DeliveryAddress;
import com.shop.repository.DeliveryAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryAddressService {
    
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    
    // 获取客户的所有收货地址
    public List<DeliveryAddress> getAddressesByCustomer(Customer customer) {
        return deliveryAddressRepository.findByCustomerOrderByIsDefaultDescCreateTimeDesc(customer);
    }
    
    // 获取客户的默认地址
    public DeliveryAddress getDefaultAddress(Customer customer) {
        return deliveryAddressRepository.findByCustomerAndIsDefaultTrue(customer).orElse(null);
    }
    
    // 添加收货地址
    @Transactional
    public DeliveryAddress addAddress(Customer customer, DeliveryAddress address) {
        address.setCustomer(customer);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        
        // 如果设置为默认地址，需要将其他地址的默认状态取消
        if (address.getIsDefault()) {
            DeliveryAddress existingDefault = getDefaultAddress(customer);
            if (existingDefault != null) {
                existingDefault.setIsDefault(false);
                deliveryAddressRepository.save(existingDefault);
            }
        }
        
        // 如果是第一个地址，自动设为默认
        if (deliveryAddressRepository.countByCustomer(customer) == 0) {
            address.setIsDefault(true);
        }
        
        return deliveryAddressRepository.save(address);
    }
    
    // 更新收货地址
    @Transactional
    public DeliveryAddress updateAddress(Long addressId, Customer customer, DeliveryAddress newAddress) {
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("无权修改此地址");
        }
        
        // 如果设置为默认地址，需要将其他地址的默认状态取消
        if (newAddress.getIsDefault() && !address.getIsDefault()) {
            DeliveryAddress existingDefault = getDefaultAddress(customer);
            if (existingDefault != null && !existingDefault.getId().equals(addressId)) {
                existingDefault.setIsDefault(false);
                deliveryAddressRepository.save(existingDefault);
            }
        }
        
        address.setReceiverName(newAddress.getReceiverName());
        address.setReceiverPhone(newAddress.getReceiverPhone());
        address.setProvince(newAddress.getProvince());
        address.setCity(newAddress.getCity());
        address.setDistrict(newAddress.getDistrict());
        address.setDetailAddress(newAddress.getDetailAddress());
        address.setIsDefault(newAddress.getIsDefault());
        address.setUpdateTime(LocalDateTime.now());
        
        return deliveryAddressRepository.save(address);
    }
    
    // 设置默认地址
    @Transactional
    public void setDefaultAddress(Long addressId, Customer customer) {
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("无权修改此地址");
        }
        
        // 取消其他默认地址
        DeliveryAddress existingDefault = getDefaultAddress(customer);
        if (existingDefault != null && !existingDefault.getId().equals(addressId)) {
            existingDefault.setIsDefault(false);
            deliveryAddressRepository.save(existingDefault);
        }
        
        address.setIsDefault(true);
        address.setUpdateTime(LocalDateTime.now());
        deliveryAddressRepository.save(address);
    }
    
    // 删除收货地址
    @Transactional
    public void deleteAddress(Long addressId, Customer customer) {
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("无权删除此地址");
        }
        
        boolean wasDefault = address.getIsDefault();
        deliveryAddressRepository.delete(address);
        
        // 如果删除的是默认地址，将第一个地址设为默认
        if (wasDefault) {
            List<DeliveryAddress> addresses = getAddressesByCustomer(customer);
            if (!addresses.isEmpty()) {
                DeliveryAddress firstAddress = addresses.get(0);
                firstAddress.setIsDefault(true);
                deliveryAddressRepository.save(firstAddress);
            }
        }
    }
    
    // 根据ID获取地址
    public DeliveryAddress getAddressById(Long addressId) {
        return deliveryAddressRepository.findById(addressId).orElse(null);
    }
}
