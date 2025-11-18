package com.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 返回一个不执行任何操作的密码编码器，用于明文密码验证
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                // 不进行编码，直接返回原始密码
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // 直接比较原始密码和存储的密码
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}