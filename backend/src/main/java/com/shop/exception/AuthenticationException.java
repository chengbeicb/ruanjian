package com.shop.exception;

public class AuthenticationException extends RuntimeException {
    
    // 默认构造函数
    public AuthenticationException() {
        super();
    }
    
    // 带消息参数的构造函数
    public AuthenticationException(String message) {
        super(message);
    }
}