package com.shop.exception;

public class ProductStatusException extends RuntimeException {
    
    // 默认构造函数
    public ProductStatusException() {
        super();
    }
    
    // 带消息参数的构造函数
    public ProductStatusException(String message) {
        super(message);
    }
}