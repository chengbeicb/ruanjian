-- =====================================
-- 升级需求包C：支付、配送、售后服务
-- 创建时间：2026-01-08
-- =====================================

-- 1. 修改订单表：增加支付相关字段
ALTER TABLE orders 
ADD COLUMN payment_status VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID(待支付)、PAID(已支付)、REFUNDED(已退款)',
ADD COLUMN payment_method VARCHAR(20) COMMENT '支付方式：BANK_CARD(银行卡)、CREDIT_CARD(信用卡)、ALIPAY(支付宝)、WECHAT(微信支付)',
ADD COLUMN payment_time DATETIME COMMENT '支付时间',
ADD COLUMN logistics_company VARCHAR(50) COMMENT '物流公司',
ADD COLUMN logistics_number VARCHAR(100) COMMENT '物流单号',
ADD COLUMN shipping_time DATETIME COMMENT '发货时间',
ADD COLUMN delivery_time DATETIME COMMENT '签收时间';

-- 2. 创建收货地址表
CREATE TABLE IF NOT EXISTS delivery_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail_address VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户收货地址表';

-- 3. 创建支付记录表
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    payment_number VARCHAR(100) UNIQUE NOT NULL COMMENT '支付流水号',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式',
    payment_amount DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    payment_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '支付状态：PENDING(处理中)、SUCCESS(成功)、FAILED(失败)',
    payment_time DATETIME COMMENT '支付时间',
    transaction_id VARCHAR(100) COMMENT '第三方交易号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_payment_number (payment_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 4. 创建物流信息表
CREATE TABLE IF NOT EXISTS logistics_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    logistics_company VARCHAR(50) NOT NULL COMMENT '物流公司',
    logistics_number VARCHAR(100) NOT NULL COMMENT '物流单号',
    current_status VARCHAR(50) COMMENT '当前物流状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_logistics_number (logistics_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流信息表';

-- 5. 创建物流跟踪记录表
CREATE TABLE IF NOT EXISTS logistics_tracks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    logistics_info_id BIGINT NOT NULL COMMENT '物流信息ID',
    track_time DATETIME NOT NULL COMMENT '跟踪时间',
    track_status VARCHAR(50) NOT NULL COMMENT '物流状态',
    track_info VARCHAR(500) NOT NULL COMMENT '物流信息描述',
    location VARCHAR(100) COMMENT '所在地',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (logistics_info_id) REFERENCES logistics_info(id) ON DELETE CASCADE,
    INDEX idx_logistics_info_id (logistics_info_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流跟踪记录表';

-- 6. 创建售后申请表
CREATE TABLE IF NOT EXISTS after_sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    after_sale_number VARCHAR(100) UNIQUE NOT NULL COMMENT '售后单号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_item_id BIGINT NOT NULL COMMENT '订单明细ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    service_type VARCHAR(20) NOT NULL COMMENT '服务类型：RETURN(退货)、REFUND(退款)、EXCHANGE(换货)',
    reason VARCHAR(50) NOT NULL COMMENT '申请原因',
    description TEXT COMMENT '问题描述',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '处理状态：PENDING(待处理)、PROCESSING(处理中)、APPROVED(已同意)、REJECTED(已拒绝)、COMPLETED(已完成)',
    seller_reply TEXT COMMENT '商家回复',
    result VARCHAR(20) COMMENT '处理结果：REFUNDED(已退款)、RETURNED(已退货)、EXCHANGED(已换货)',
    refund_amount DECIMAL(10, 2) COMMENT '退款金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    complete_time DATETIME COMMENT '完成时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_after_sale_number (after_sale_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请表';

-- 7. 创建售后申请图片表
CREATE TABLE IF NOT EXISTS after_sale_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    after_sale_id BIGINT NOT NULL COMMENT '售后申请ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    image_order INT DEFAULT 0 COMMENT '图片顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (after_sale_id) REFERENCES after_sales(id) ON DELETE CASCADE,
    INDEX idx_after_sale_id (after_sale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请图片表';

-- 8. 插入测试数据：默认收货地址
INSERT INTO delivery_addresses (customer_id, receiver_name, receiver_phone, province, city, district, detail_address, is_default) 
VALUES 
(1, '张三', '13800138000', '北京市', '朝阳区', '朝阳区', '建国路88号SOHO现代城A座1001室', TRUE),
(1, '张三', '13800138001', '上海市', '浦东新区', '浦东新区', '世纪大道1号环球金融中心', FALSE),
(2, '李四', '13900139000', '广东省', '深圳市', '南山区', '科技园南区高新南一道中国科技开发院', TRUE);

-- 9. 插入测试数据：模拟物流公司
-- 这里可以预设一些物流公司信息，实际使用时从订单表关联

SELECT '数据库升级脚本执行完成！' AS message;
