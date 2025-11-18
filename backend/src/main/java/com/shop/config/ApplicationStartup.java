package com.shop.config;

import com.shop.controller.SellerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * 应用启动时自动初始化卖家账号
 */
@Component
public class ApplicationStartup implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(ApplicationStartup.class.getName());

    @Autowired
    private SellerController sellerController;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("正在初始化默认卖家账号...");
        try {
            // 调用初始化接口创建默认卖家账号
            sellerController.initSeller();
            logger.info("默认卖家账号初始化完成");
        } catch (Exception e) {
            logger.warning("默认卖家账号初始化失败或已存在: " + e.getMessage());
        }
    }
}