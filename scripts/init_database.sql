-- ============================================
-- 简单在线购物系统 - 数据库初始化脚本
-- 创建时间: 2025-11-27
-- 说明: 此脚本会创建数据库和所有必需的表
--      如果数据库或表已存在则跳过创建
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS shopping_system 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE shopping_system;

-- ============================================
-- 卖家表
-- ============================================
CREATE TABLE IF NOT EXISTS sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '卖家ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    store_name VARCHAR(100) COMMENT '店铺名称',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    active BOOLEAN DEFAULT TRUE COMMENT '账号是否激活',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卖家表';

-- ============================================
-- 客户表
-- ============================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(20) COMMENT '手机号',
    default_address VARCHAR(255) COMMENT '默认地址',
    active BOOLEAN DEFAULT TRUE COMMENT '账号是否激活',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- ============================================
-- 商品表
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    image_urls TEXT COMMENT '商品图片URL（多张用逗号分隔）',
    status VARCHAR(20) DEFAULT 'UNPUBLISHED' COMMENT '商品状态: AVAILABLE-可购买, SOLD-已售出, FROZEN-已冻结, UNPUBLISHED-已下架',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    stock_quantity INT COMMENT '库存数量',
    category_level1 VARCHAR(50) COMMENT '一级分类',
    category_level2 VARCHAR(50) COMMENT '二级分类',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_category (category_level1, category_level2),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 订单表（升级需求包B）
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态: PENDING-客户下单, CONFIRMED-商家确认, PREPARING-备货完成, SHIPPING-开始发货, COMPLETED-交易完成, CANCELLED-已取消',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    receiver_name VARCHAR(100) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    shipping_address VARCHAR(255) COMMENT '收货地址',
    remark VARCHAR(500) COMMENT '订单备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    cancel_role VARCHAR(20) COMMENT '取消人角色: CUSTOMER-客户, SELLER-商家',
    INDEX idx_order_number (order_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 订单商品明细表（升级需求包B新增）
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单商品ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

-- ============================================
-- 收藏表（升级需求包B新增）
-- ============================================
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_customer_product (customer_id, product_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_product_id (product_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 购物车表（升级需求包B新增）
-- ============================================
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '商品数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入购物车时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_customer_product (customer_id, product_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_product_id (product_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 初始化测试数据
-- ============================================

-- 插入测试卖家账号（仅当不存在时）
-- 用户名: admin, 密码: admin123
INSERT INTO sellers (username, password, email, store_name, contact_phone, active) 
SELECT 'admin', 'admin123', 'admin@shop.com', '测试商店', '13800138000', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sellers WHERE username = 'admin');

-- 插入测试客户账号（仅当不存在时）
-- 用户名: customer1, 密码: 123456
INSERT INTO customers (username, password, phone, active) 
SELECT 'customer1', '123456', '13900139000', TRUE
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE username = 'customer1');

-- ============================================
-- 脚本执行完成
-- ============================================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('数据库: ', DATABASE()) AS current_database;
SELECT CONCAT('卖家表记录数: ', COUNT(*)) AS seller_count FROM sellers;
SELECT CONCAT('客户表记录数: ', COUNT(*)) AS customer_count FROM customers;
SELECT CONCAT('商品表记录数: ', COUNT(*)) AS product_count FROM products;
SELECT CONCAT('订单表记录数: ', COUNT(*)) AS order_count FROM orders;
SELECT CONCAT('收藏表记录数: ', COUNT(*)) AS favorite_count FROM favorites;
SELECT CONCAT('购物车表记录数: ', COUNT(*)) AS cart_count FROM shopping_cart;
