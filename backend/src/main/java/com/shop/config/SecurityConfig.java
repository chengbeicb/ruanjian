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

import com.shop.entity.Seller;
import com.shop.exception.SellerNotFoundException;
import com.shop.service.SellerService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SellerService sellerService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and() // 添加CORS支持
            .csrf().disable() // 禁用CSRF保护，适用于API
            .authorizeHttpRequests(authorize -> authorize
                .antMatchers("/seller/login", "/seller/register", "/seller/init").permitAll()
                .antMatchers("/products", "/products/**").permitAll()
                .antMatchers("/purchase-intents").permitAll()
                .antMatchers("/seller/password", "/seller/me", "/seller").authenticated()
                .antMatchers("/products/seller", "/products/**/publish", "/products/**/unpublish").authenticated()
                .antMatchers("/purchase-intents/**").authenticated()
                .antMatchers("/files/upload").permitAll()
                .antMatchers("/uploads/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic() // 使用HTTP Basic认证
            .and()
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 无状态会话
        return http.build();
    }

    // 添加全局CORS配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
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