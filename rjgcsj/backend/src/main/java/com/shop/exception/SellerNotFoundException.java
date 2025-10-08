package com.shop.exception;

public class SellerNotFoundException extends RuntimeException {
    
    // 默认构造函数
    public SellerNotFoundException() {
        super();
    }
    
    // 带消息参数的构造函数
    public SellerNotFoundException(String message) {
        super(message);
    }
}