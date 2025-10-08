package com.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.shop.entity.Seller;
import com.shop.exception.SellerNotFoundException;
import com.shop.service.SellerService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SellerService sellerService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 移除passwordEncoder()方法

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                Seller seller = sellerService.getSellerByUsername(username);
                
                return org.springframework.security.core.userdetails.User.builder()
                    .username(seller.getUsername())
                    .password(seller.getPassword())
                    .roles("SELLER")
                    .build();
            } catch (SellerNotFoundException e) {
                throw new UsernameNotFoundException("User not found: " + username, e);
            }
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and() // 添加CORS支持
            .csrf().disable() // 禁用CSRF保护，适用于API
            .authorizeRequests()
                .antMatchers("/seller/login", "/seller/register", "/seller/init").permitAll() // 移除/api前缀
                .antMatchers("/products", "/products/**").permitAll() // 移除/api前缀
                .antMatchers("/purchase-intents").permitAll() // 移除/api前缀
                .antMatchers("/seller/password", "/seller/me", "/seller").authenticated() // 移除/api前缀
                .antMatchers("/products/seller", "/products/**/publish", "/products/**/unpublish").authenticated() // 移除/api前缀
                .antMatchers("/purchase-intents/**").authenticated() // 移除/api前缀
                .antMatchers("/files/upload").permitAll() // 允许文件上传API的访问
                .antMatchers("/uploads/**").permitAll() // 允许访问上传的文件
                .anyRequest().authenticated()
            .and()
            .httpBasic() // 使用HTTP Basic认证
            .and()
            .sessionManagement()
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS); // 无状态会话
    }

    // 添加全局CORS配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 修改前
        configuration.addAllowedOrigin("http://localhost:8081");
        
        // 修改后
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*"); // 允许所有HTTP方法
        configuration.addAllowedHeader("*"); // 允许所有请求头
        configuration.setAllowCredentials(true); // 允许携带凭证（如cookie）
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
        return source;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用注入的passwordEncoder，而不是调用已移除的方法
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder);
    }
}