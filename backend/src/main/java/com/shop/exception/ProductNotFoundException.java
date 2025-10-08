package com.shop.exception;

public class ProductNotFoundException extends RuntimeException {
    
    // 默认构造函数
    public ProductNotFoundException() {
        super();
    }
    
    // 带消息参数的构造函数
    public ProductNotFoundException(String message) {
        super(message);
    }
}