package com.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.shop.entity.Customer;
import com.shop.entity.Seller;
import com.shop.exception.AuthenticationException;
import com.shop.exception.SellerNotFoundException;
import com.shop.service.CustomerService;
import com.shop.service.SellerService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SellerService sellerService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 先尝试作为卖家登录
            try {
                Seller seller = sellerService.getSellerByUsername(username);
                
                return org.springframework.security.core.userdetails.User.builder()
                    .username(seller.getUsername())
                    .password(seller.getPassword())
                    .roles("SELLER")
                    .build();
            } catch (SellerNotFoundException e) {
                // 如果卖家不存在，尝试作为客户登录
                try {
                    Customer customer = customerService.getCustomerByUsername(username);
                    
                    return org.springframework.security.core.userdetails.User.builder()
                        .username(customer.getUsername())
                        .password(customer.getPassword())
                        .roles("CUSTOMER")
                        .build();
                } catch (AuthenticationException ex) {
                    throw new UsernameNotFoundException("User not found: " + username, ex);
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 显式应用CORS配置
            .csrf(csrf -> csrf.disable()) // 禁用CSRF保护，适用于API
            .authorizeHttpRequests(authorize -> authorize
                // 优先配置所有允许匿名访问的公共端点
                .antMatchers(
                    // -- SPA 静态资源 --
                    "/",
                    "/index.html",
                    "/favicon.ico",
                    "/*.css",
                    "/*.js",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/fonts/**",
                    "/images/**", // 新增：允许访问图片
                    
                    // -- API 公共端点 --
                    "/api/seller/login", 
                    "/api/seller/register", 
                    "/api/seller/init",
                    "/api/customer/register", 
                    "/api/customer/login",
                    "/api/products",       // 允许访问商品列表
                    "/api/products/**",    // 允许访问单个商品详情
                    "/api/files/upload",
                    "/uploads/**"
                ).permitAll()
                
                // 对于所有其他请求，要求必须经过身份验证
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}) // 启用HTTP Basic认证
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 无状态会话
        return http.build();
    }

    // 添加全局CORS配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 使用 allowedOriginPatterns 替代 "*"，以支持 allowCredentials
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("http://127.0.0.1:*");
        configuration.addAllowedMethod("*"); // 允许所有HTTP方法
        configuration.addAllowedHeader("*"); // 允许所有请求头
        configuration.setAllowCredentials(true); // 允许携带凭证（如cookie）
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
}