package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.shop.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA配置类，用于配置Spring Data JPA
    // basePackages指定了仓库接口所在的包路径
    // @EnableTransactionManagement启用事务管理
}