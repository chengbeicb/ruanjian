-- ============================================
-- 购买意向数据迁移脚本（可选）
-- 警告：此脚本仅在旧系统有数据需要保留时使用
-- ============================================

-- 前提条件：purchase_intents 表还存在且有数据

-- 第一步：备份旧数据
CREATE TABLE IF NOT EXISTS purchase_intents_backup AS 
SELECT * FROM purchase_intents;

-- 第二步：将购买意向转换为订单
-- 注意：由于结构差异，需要做以下假设：
-- 1. 每个购买意向转换为一个订单（单商品）
-- 2. 订单状态根据 completed/canceled 字段映射
-- 3. 使用当前商品价格作为订单金额（历史价格无法获取）

INSERT INTO orders (
    order_number,
    customer_id,
    status,
    total_amount,
    receiver_name,
    receiver_phone,
    shipping_address,
    remark,
    create_time,
    update_time,
    cancel_time,
    cancel_reason,
    cancel_role
)
SELECT 
    CONCAT('MIGRATED', LPAD(pi.id, 10, '0')) AS order_number,  -- 生成订单编号
    pi.customer_id,
    CASE 
        WHEN pi.completed = TRUE THEN 'COMPLETED'
        WHEN pi.canceled = TRUE THEN 'CANCELLED'
        ELSE 'PENDING'
    END AS status,
    p.price AS total_amount,  -- 使用当前价格（可能不准确）
    pi.buyer_name AS receiver_name,
    pi.buyer_phone AS receiver_phone,
    pi.buyer_address AS shipping_address,
    '从旧系统迁移' AS remark,
    pi.create_time,
    pi.create_time AS update_time,
    NULL AS cancel_time,
    CASE WHEN pi.canceled = TRUE THEN '旧系统取消' ELSE NULL END AS cancel_reason,
    CASE WHEN pi.canceled = TRUE THEN 'CUSTOMER' ELSE NULL END AS cancel_role
FROM purchase_intents pi
JOIN products p ON pi.product_id = p.id
WHERE pi.customer_id IS NOT NULL;  -- 只迁移有客户ID的记录

-- 第三步：创建订单明细
INSERT INTO order_items (
    order_id,
    product_id,
    quantity,
    unit_price,
    subtotal
)
SELECT 
    o.id AS order_id,
    p.id AS product_id,
    1 AS quantity,  -- 假设数量为1
    p.price AS unit_price,
    p.price AS subtotal
FROM orders o
JOIN purchase_intents_backup pi ON o.order_number = CONCAT('MIGRATED', LPAD(pi.id, 10, '0'))
JOIN products p ON pi.product_id = p.id;

-- 第四步：删除旧表（谨慎操作！）
-- DROP TABLE IF EXISTS purchase_intents;

-- 查看迁移结果
SELECT '迁移完成！' AS message;
SELECT CONCAT('已迁移订单数: ', COUNT(*)) AS migrated_count 
FROM orders WHERE order_number LIKE 'MIGRATED%';

SELECT '注意事项：' AS warning;
SELECT '1. 订单金额使用的是当前商品价格，可能与实际购买时价格不同' AS note_1;
SELECT '2. 所有订单数量默认为1，如果旧系统有数量字段需要手动调整' AS note_2;
SELECT '3. 已取消的订单状态为CANCELLED，已完成的为COMPLETED，其他为PENDING' AS note_3;
SELECT '4. 迁移的订单编号格式为 MIGRATED + 原意向ID' AS note_4;
