package com.shop.exception;

public class PurchaseIntentNotFoundException extends RuntimeException {
    
    // 默认构造函数
    public PurchaseIntentNotFoundException() {
        super();
    }
    
    // 带消息参数的构造函数
    public PurchaseIntentNotFoundException(String message) {
        super(message);
    }
}