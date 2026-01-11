-- =====================================================
-- PinShop 测试数据
-- 包含秒杀商品、拼团商品等完整测试数据
-- =====================================================

USE pinshop;

-- 清理旧数据
TRUNCATE TABLE group_session;
TRUNCATE TABLE group_rule;
TRUNCATE TABLE flash_item;
TRUNCATE TABLE flash_event;
TRUNCATE TABLE order_item;
TRUNCATE TABLE trade_order;
TRUNCATE TABLE delivery_order;
TRUNCATE TABLE product_sku;
TRUNCATE TABLE product_spu;

-- =====================================================
-- 1. 商品SPU数据
-- =====================================================

INSERT INTO `product_spu` (`id`, `name`, `category_id`, `main_image`, `detail_images`, `status`) VALUES
(1, 'iPhone 15 Pro Max 256GB', 1, 'https://picsum.photos/400/400?random=101', '["https://picsum.photos/750/750?random=101","https://picsum.photos/750/750?random=102"]', 1),
(2, '小米14 Ultra 512GB', 1, 'https://picsum.photos/400/400?random=102', '["https://picsum.photos/750/750?random=103","https://picsum.photos/750/750?random=104"]', 1),
(3, 'Tesla Model Y 玩具车', 2, 'https://picsum.photos/400/400?random=103', '["https://picsum.photos/750/750?random=105","https://picsum.photos/750/750?random=106"]', 1),
(4, '戴森吹风机 HD08', 3, 'https://picsum.photos/400/400?random=104', '["https://picsum.photos/750/750?random=107","https://picsum.photos/750/750?random=108"]', 1),
(5, 'AirPods Pro 2', 1, 'https://picsum.photos/400/400?random=105', '["https://picsum.photos/750/750?random=109","https://picsum.photos/750/750?random=110"]', 1),
(6, 'SK-II 神仙水 230ml', 4, 'https://picsum.photos/400/400?random=106', '["https://picsum.photos/750/750?random=111","https://picsum.photos/750/750?random=112"]', 1),
(7, 'Nike Air Max 270', 5, 'https://picsum.photos/400/400?random=107', '["https://picsum.photos/750/750?random=113","https://picsum.photos/750/750?random=114"]', 1),
(8, 'iPad Pro 11寸 M4', 1, 'https://picsum.photos/400/400?random=108', '["https://picsum.photos/750/750?random=115","https://picsum.photos/750/750?random=116"]', 1),
(9, '索尼WH-1000XM5耳机', 1, 'https://picsum.photos/400/400?random=109', '["https://picsum.photos/750/750?random=117","https://picsum.photos/750/750?random=118"]', 1),
(10, '乐高积木 跑车系列', 2, 'https://picsum.photos/400/400?random=110', '["https://picsum.photos/750/750?random=119","https://picsum.photos/750/750?random=120"]', 1);

-- =====================================================
-- 2. 商品SKU数据
-- =====================================================

INSERT INTO `product_sku` (`id`, `spu_id`, `specs`, `market_price`, `sale_price`, `stock`) VALUES
-- iPhone 15 Pro Max
(1, 1, '{"color": "原色钛金属", "storage": "256GB"}', 9999.00, 8999.00, 50),
(2, 1, '{"color": "蓝色钛金属", "storage": "256GB"}', 9999.00, 8999.00, 30),
-- 小米14 Ultra
(3, 2, '{"color": "黑色", "storage": "512GB"}', 6999.00, 5999.00, 100),
(4, 2, '{"color": "白色", "storage": "512GB"}', 6999.00, 5999.00, 80),
-- Tesla玩具车
(5, 3, '{"color": "红色"}', 399.00, 299.00, 200),
-- 戴森吹风机
(6, 4, '{"color": "紫色"}', 3690.00, 2990.00, 60),
-- AirPods Pro 2
(7, 5, '{"color": "白色"}', 1899.00, 1699.00, 150),
-- SK-II神仙水
(8, 6, '{"spec": "230ml"}', 1690.00, 1290.00, 90),
-- Nike鞋
(9, 7, '{"size": "42", "color": "黑白"}', 1299.00, 999.00, 120),
-- iPad Pro
(10, 8, '{"color": "深空灰", "storage": "256GB"}', 7999.00, 7199.00, 70),
-- 索尼耳机
(11, 9, '{"color": "黑色"}', 2499.00, 1999.00, 85),
-- 乐高积木
(12, 10, '{"age": "18+"}', 2999.00, 2299.00, 40);

-- =====================================================
-- 3. 秒杀活动数据
-- =====================================================

-- 创建今日三个时段的秒杀活动
INSERT INTO `flash_event` (`id`, `title`, `start_time`, `end_time`, `status`) VALUES
(1, '早上8点场', CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 12:00:00'), 2),
(2, '中午12点场', CONCAT(CURDATE(), ' 12:00:00'), CONCAT(CURDATE(), ' 20:00:00'), 1),
(3, '晚上8点场', CONCAT(CURDATE(), ' 20:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 20:00:00'), INTERVAL 4 HOUR), 0);

-- =====================================================
-- 4. 秒杀商品数据
-- =====================================================

-- 12点场秒杀商品（当前进行中）
INSERT INTO `flash_item` (`event_id`, `sku_id`, `flash_price`, `flash_stock`, `lock_stock`, `limit_per_user`) VALUES
(2, 1, 6999.00, 20, 0, 1),  -- iPhone秒杀
(2, 3, 4999.00, 50, 0, 1),  -- 小米秒杀
(2, 5, 199.00, 100, 0, 2),  -- Tesla玩具车秒杀
(2, 6, 2199.00, 30, 0, 1),  -- 戴森秒杀
(2, 7, 1399.00, 60, 0, 2),  -- AirPods秒杀
(2, 8, 890.00, 40, 0, 1),   -- SK-II秒杀
(2, 11, 1599.00, 35, 0, 1); -- 索尼耳机秒杀

-- 8点场秒杀商品（已结束）
INSERT INTO `flash_item` (`event_id`, `sku_id`, `flash_price`, `flash_stock`, `lock_stock`, `limit_per_user`) VALUES
(1, 2, 6999.00, 15, 0, 1),
(1, 4, 5499.00, 40, 0, 1);

-- 20点场秒杀商品（即将开始）
INSERT INTO `flash_item` (`event_id`, `sku_id`, `flash_price`, `flash_stock`, `lock_stock`, `limit_per_user`) VALUES
(3, 9, 799.00, 50, 0, 1),
(3, 10, 6599.00, 25, 0, 1),
(3, 12, 1899.00, 20, 0, 1);

-- =====================================================
-- 5. 拼团规则数据
-- =====================================================

INSERT INTO `group_rule` (`sku_id`, `group_price`, `member_count`, `duration_hours`, `status`) VALUES
(5, 249.00, 2, 24, 1),   -- Tesla玩具车2人拼团
(6, 2599.00, 3, 24, 1),  -- 戴森3人拼团
(7, 1299.00, 2, 24, 1),  -- AirPods 2人拼团
(8, 1090.00, 3, 24, 1),  -- SK-II 3人拼团
(9, 899.00, 2, 24, 1),   -- Nike鞋2人拼团
(11, 1799.00, 2, 24, 1); -- 索尼耳机2人拼团

-- =====================================================
-- 6. 模拟拼团会话
-- =====================================================

INSERT INTO `group_session` (`rule_id`, `initiator_id`, `status`, `current_count`, `expire_time`) VALUES
-- 进行中的拼团
(1, 1, 0, 1, DATE_ADD(NOW(), INTERVAL 12 HOUR)),  -- Tesla拼团还差1人
(2, 1, 0, 2, DATE_ADD(NOW(), INTERVAL 18 HOUR)),  -- 戴森拼团还差1人
(3, 2, 0, 1, DATE_ADD(NOW(), INTERVAL 20 HOUR)),  -- AirPods拼团还差1人
-- 成功的拼团
(5, 1, 1, 2, DATE_ADD(NOW(), INTERVAL -2 HOUR));

-- =====================================================
-- 数据统计信息
-- =====================================================

SELECT '测试数据插入完成!' AS message;
SELECT COUNT(*) AS '商品SPU数量' FROM product_spu;
SELECT COUNT(*) AS '商品SKU数量' FROM product_sku;
SELECT COUNT(*) AS '秒杀活动数量' FROM flash_event;
SELECT COUNT(*) AS '秒杀商品数量' FROM flash_item;
SELECT COUNT(*) AS '拼团规则数量' FROM group_rule;
SELECT COUNT(*) AS '拼团会话数量' FROM group_session;
