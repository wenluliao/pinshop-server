-- =====================================================
-- 1. User & Base Domain
-- =====================================================

-- User table (C-side)
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT 'User ID',
  `openid` varchar(64) NOT NULL COMMENT 'WeChat OpenID',
  `nickname` varchar(64) COMMENT 'User nickname',
  `avatar_url` varchar(255) COMMENT 'Avatar URL',
  `phone` varchar(20) COMMENT 'Mobile phone',
  `status` tinyint DEFAULT 1 COMMENT '1:Normal 0:Disabled',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY `uk_openid` (`openid`),
  INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';

-- User address book
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `receiver_name` varchar(32) NOT NULL COMMENT 'Receiver name',
  `receiver_phone` varchar(20) NOT NULL COMMENT 'Receiver phone',
  `province` varchar(32) COMMENT 'Province',
  `city` varchar(32) COMMENT 'City',
  `district` varchar(32) COMMENT 'District',
  `detail_addr` varchar(255) COMMENT 'Detail address',
  `is_default` tinyint DEFAULT 0 COMMENT '1:Default 0:Not default',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Address';

-- =====================================================
-- 2. Product & Inventory Domain
-- =====================================================

-- Product SPU (Standard Product Unit)
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT 'Product name',
  `category_id` int COMMENT 'Category ID',
  `main_image` varchar(255) COMMENT 'Main image URL',
  `detail_images` json COMMENT 'Detail images list',
  `status` tinyint DEFAULT 0 COMMENT '0:Draft 1:Online 2:Offline',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX `idx_status` (`status`),
  INDEX `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product SPU';

-- Product SKU (Stock Keeping Unit)
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `spu_id` bigint NOT NULL COMMENT 'SPU ID',
  `specs` json COMMENT 'Specs KV, e.g. {"color":"red", "size":"L"}',
  `market_price` decimal(10,2) COMMENT 'Market price',
  `sale_price` decimal(10,2) COMMENT 'Sale price',
  `stock` int NOT NULL DEFAULT 0 COMMENT 'Available stock',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX `idx_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product SKU';

-- Stock ledger (financial audit)
DROP TABLE IF EXISTS `stock_ledger`;
CREATE TABLE `stock_ledger` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `biz_type` varchar(32) NOT NULL COMMENT 'Business type: ORDER_OUT, STOCK_IN, REFUND_IN',
  `biz_id` varchar(64) NOT NULL COMMENT 'Related order ID',
  `change_num` int NOT NULL COMMENT 'Change number, +/-',
  `balance_snapshot` int NOT NULL COMMENT 'Balance after change',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  INDEX `idx_sku_time` (`sku_id`, `create_time`),
  INDEX `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stock Ledger';

-- =====================================================
-- 3. Marketing Domain (Flash Sale & Group Buy)
-- =====================================================

-- Flash sale event
DROP TABLE IF EXISTS `flash_event`;
CREATE TABLE `flash_event` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `title` varchar(64) COMMENT 'Event title, e.g. "Double 11 8PM"',
  `start_time` datetime NOT NULL COMMENT 'Start time',
  `end_time` datetime NOT NULL COMMENT 'End time',
  `status` tinyint DEFAULT 0 COMMENT '0:Not started 1:In progress 2:Ended',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  INDEX `idx_time` (`start_time`, `end_time`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flash Sale Event';

-- Flash sale item (independent stock)
DROP TABLE IF EXISTS `flash_item`;
CREATE TABLE `flash_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `event_id` bigint NOT NULL COMMENT 'Event ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `flash_price` decimal(10,2) NOT NULL COMMENT 'Flash sale price',
  `flash_stock` int NOT NULL COMMENT 'Allocated stock for flash sale',
  `lock_stock` int DEFAULT 0 COMMENT 'Locked stock',
  `limit_per_user` int DEFAULT 1 COMMENT 'Purchase limit per user',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY `uk_event_sku` (`event_id`, `sku_id`),
  INDEX `idx_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flash Sale Item';

-- Group buy rule
DROP TABLE IF EXISTS `group_rule`;
CREATE TABLE `group_rule` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `group_price` decimal(10,2) NOT NULL COMMENT 'Group buy price',
  `member_count` int NOT NULL DEFAULT 2 COMMENT 'Members required to form group',
  `duration_hours` int DEFAULT 24 COMMENT 'Duration in hours',
  `status` tinyint DEFAULT 1 COMMENT '1:Active 0:Inactive',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX `idx_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Group Buy Rule';

-- Group session (actual groups)
DROP TABLE IF EXISTS `group_session`;
CREATE TABLE `group_session` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `rule_id` bigint NOT NULL COMMENT 'Rule ID',
  `initiator_id` bigint NOT NULL COMMENT 'Group leader ID',
  `status` tinyint DEFAULT 0 COMMENT '0:In progress 1:Success 2:Failed',
  `current_count` int DEFAULT 1 COMMENT 'Current member count',
  `expire_time` datetime NOT NULL COMMENT 'Expiration time',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX `idx_status` (`status`),
  INDEX `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Group Session';

-- =====================================================
-- 4. Trade & Fulfillment Domain
-- =====================================================

-- Order main table
DROP TABLE IF EXISTS `trade_order`;
CREATE TABLE `trade_order` (
  `id` bigint PRIMARY KEY COMMENT 'Distributed ID (Snowflake)',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT 'Total amount',
  `pay_amount` decimal(10,2) NOT NULL COMMENT 'Payable amount',
  `status` tinyint DEFAULT 10 COMMENT '10:Unpaid 20:Paid/GroupSuccess 30:Shipped 40:Completed 50:Cancelled 60:AfterSale',
  `order_type` varchar(16) COMMENT 'NORMAL, FLASH, GROUP',
  `marketing_id` bigint COMMENT 'Related flash sale ID or group session ID',
  `receiver_info` json NOT NULL COMMENT 'Receiver snapshot {name, phone, address}',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `pay_time` datetime(3),
  `extra_json` json COMMENT 'Extended information',
  INDEX `idx_user` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trade Order';

-- Order items
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT 'Order ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_name` varchar(128) COMMENT 'SKU name',
  `price` decimal(10,2) COMMENT 'Unit price at purchase',
  `quantity` int NOT NULL COMMENT 'Quantity',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  INDEX `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order Items';

-- Delivery order
DROP TABLE IF EXISTS `delivery_order`;
CREATE TABLE `delivery_order` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT 'Order ID',
  `logistics_company` varchar(64) COMMENT 'Logistics company: SF, YTO, etc.',
  `tracking_no` varchar(64) COMMENT 'Tracking number',
  `ship_time` datetime DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Ship time',
  `status` tinyint DEFAULT 0 COMMENT '0:Shipped 1:Delivered',
  `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX `idx_order` (`order_id`),
  INDEX `idx_tracking` (`tracking_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Delivery Order';

-- =====================================================
-- Initial Data
-- =====================================================

-- Insert sample test data
INSERT INTO `user` (`openid`, `nickname`, `phone`) VALUES
  ('test_openid_001', 'Test User 1', '13800138001'),
  ('test_openid_002', 'Test User 2', '13800138002');

INSERT INTO `product_spu` (`name`, `status`) VALUES
  ('iPhone 15 Pro Max', 1),
  ('Tesla Model Y Toy Car', 1);

INSERT INTO `product_sku` (`spu_id`, `sale_price`, `stock`) VALUES
  (1, 999.00, 100),
  (2, 99.00, 500);
