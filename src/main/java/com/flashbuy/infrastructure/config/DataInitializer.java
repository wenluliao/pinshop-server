package com.flashbuy.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 测试数据初始化器
 * 仅在开发环境下运行
 */
@Component
@Order(1)
@ConditionalOnBean(DataSource.class)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;

    public DataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            log.info("开始初始化测试数据...");

            // 检查是否已有数据
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM product_spu");
            if (rs.next() && rs.getInt(1) > 0) {
                log.info("数据库已有数据，跳过初始化");
                return;
            }

            // 清理旧数据
            stmt.execute("TRUNCATE TABLE group_session");
            stmt.execute("TRUNCATE TABLE group_rule");
            stmt.execute("TRUNCATE TABLE flash_item");
            stmt.execute("TRUNCATE TABLE flash_event");
            stmt.execute("TRUNCATE TABLE order_item");
            stmt.execute("TRUNCATE TABLE trade_order");
            stmt.execute("TRUNCATE TABLE delivery_order");
            stmt.execute("TRUNCATE TABLE product_sku");
            stmt.execute("TRUNCATE TABLE product_spu");

            // 插入商品SPU
            String spuSql = """
                INSERT INTO product_spu (id, name, category_id, main_image, detail_images, status) VALUES
                (1, 'iPhone 15 Pro Max 256GB', 1, 'https://picsum.photos/400/400?random=101',
                 '["https://picsum.photos/750/750?random=101","https://picsum.photos/750/750?random=102"]', 1),
                (2, '小米14 Ultra 512GB', 1, 'https://picsum.photos/400/400?random=102',
                 '["https://picsum.photos/750/750?random=103","https://picsum.photos/750/750?random=104"]', 1),
                (3, 'Tesla Model Y 玩具车', 2, 'https://picsum.photos/400/400?random=103',
                 '["https://picsum.photos/750/750?random=105","https://picsum.photos/750/750?random=106"]', 1),
                (4, '戴森吹风机 HD08', 3, 'https://picsum.photos/400/400?random=104',
                 '["https://picsum.photos/750/750?random=107","https://picsum.photos/750/750?random=108"]', 1),
                (5, 'AirPods Pro 2', 1, 'https://picsum.photos/400/400?random=105',
                 '["https://picsum.photos/750/750?random=109","https://picsum.photos/750/750?random=110"]', 1),
                (6, 'SK-II 神仙水 230ml', 4, 'https://picsum.photos/400/400?random=106',
                 '["https://picsum.photos/750/750?random=111","https://picsum.photos/750/750?random=112"]', 1),
                (7, 'Nike Air Max 270', 5, 'https://picsum.photos/400/400?random=107',
                 '["https://picsum.photos/750/750?random=113","https://picsum.photos/750/750?random=114"]', 1),
                (8, 'iPad Pro 11寸 M4', 1, 'https://picsum.photos/400/400?random=108',
                 '["https://picsum.photos/750/750?random=115","https://picsum.photos/750/750?random=116"]', 1),
                (9, '索尼WH-1000XM5耳机', 1, 'https://picsum.photos/400/400?random=109',
                 '["https://picsum.photos/750/750?random=117","https://picsum.photos/750/750?random=118"]', 1),
                (10, '乐高积木 跑车系列', 2, 'https://picsum.photos/400/400?random=110',
                 '["https://picsum.photos/750/750?random=119","https://picsum.photos/750/750?random=120"]', 1)
                """;
            stmt.execute(spuSql);
            log.info("已插入10个商品SPU");

            // 插入商品SKU
            String skuSql = """
                INSERT INTO product_sku (id, spu_id, specs, market_price, sale_price, stock) VALUES
                (1, 1, '{"color": "原色钛金属", "storage": "256GB"}', 9999.00, 8999.00, 50),
                (2, 1, '{"color": "蓝色钛金属", "storage": "256GB"}', 9999.00, 8999.00, 30),
                (3, 2, '{"color": "黑色", "storage": "512GB"}', 6999.00, 5999.00, 100),
                (4, 2, '{"color": "白色", "storage": "512GB"}', 6999.00, 5999.00, 80),
                (5, 3, '{"color": "红色"}', 399.00, 299.00, 200),
                (6, 4, '{"color": "紫色"}', 3690.00, 2990.00, 60),
                (7, 5, '{"color": "白色"}', 1899.00, 1699.00, 150),
                (8, 6, '{"spec": "230ml"}', 1690.00, 1290.00, 90),
                (9, 7, '{"size": "42", "color": "黑白"}', 1299.00, 999.00, 120),
                (10, 8, '{"color": "深空灰", "storage": "256GB"}', 7999.00, 7199.00, 70),
                (11, 9, '{"color": "黑色"}', 2499.00, 1999.00, 85),
                (12, 10, '{"age": "18+"}', 2999.00, 2299.00, 40)
                """;
            stmt.execute(skuSql);
            log.info("已插入12个商品SKU");

            // 创建秒杀活动（今日三个时段）
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String eventSql = String.format("""
                INSERT INTO flash_event (id, title, start_time, end_time, status) VALUES
                (1, '早上8点场', '%s 08:00:00', '%s 12:00:00', 2),
                (2, '中午12点场', '%s 12:00:00', '%s 20:00:00', 1),
                (3, '晚上8点场', '%s 20:00:00', DATE_ADD('%s 20:00:00', INTERVAL 4 HOUR), 0)
                """, today, today, today, today, today, today);
            stmt.execute(eventSql);
            log.info("已插入3个秒杀活动");

            // 插入秒杀商品（12点场当前进行中）
            String flashSql = """
                INSERT INTO flash_item (event_id, sku_id, flash_price, flash_stock, lock_stock, limit_per_user) VALUES
                (2, 1, 6999.00, 20, 0, 1),
                (2, 3, 4999.00, 50, 0, 1),
                (2, 5, 199.00, 100, 0, 2),
                (2, 6, 2199.00, 30, 0, 1),
                (2, 7, 1399.00, 60, 0, 2),
                (2, 8, 890.00, 40, 0, 1),
                (2, 11, 1599.00, 35, 0, 1),
                (1, 2, 6999.00, 15, 0, 1),
                (1, 4, 5499.00, 40, 0, 1),
                (3, 9, 799.00, 50, 0, 1),
                (3, 10, 6599.00, 25, 0, 1),
                (3, 12, 1899.00, 20, 0, 1)
                """;
            stmt.execute(flashSql);
            log.info("已插入12个秒杀商品");

            // 插入拼团规则
            String groupRuleSql = """
                INSERT INTO group_rule (sku_id, group_price, member_count, duration_hours, status) VALUES
                (5, 249.00, 2, 24, 1),
                (6, 2599.00, 3, 24, 1),
                (7, 1299.00, 2, 24, 1),
                (8, 1090.00, 3, 24, 1),
                (9, 899.00, 2, 24, 1),
                (11, 1799.00, 2, 24, 1)
                """;
            stmt.execute(groupRuleSql);
            log.info("已插入6个拼团规则");

            log.info("测试数据初始化完成！");
        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
            throw e;
        }
    }
}
