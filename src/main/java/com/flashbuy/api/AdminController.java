package com.flashbuy.api;

import com.flashbuy.application.admin.AdminDashboardService;
import com.flashbuy.application.admin.DashboardData;
import com.flashbuy.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Admin Dashboard API Controller
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminDashboardService adminDashboardService;
    private final DataSource dataSource;

    public AdminController(AdminDashboardService adminDashboardService, DataSource dataSource) {
        this.adminDashboardService = adminDashboardService;
        this.dataSource = dataSource;
    }

    /**
     * Get dashboard data
     * GET /api/v1/admin/dashboard
     */
    @GetMapping("/dashboard")
    public Result<DashboardData> getDashboard() {
        DashboardData data = adminDashboardService.getDashboard();
        return Result.ok(data);
    }

    /**
     * 初始化测试数据
     * POST /api/v1/admin/init-test-data
     */
    @PostMapping("/init-test-data")
    public Result<String> initTestData() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            log.info("开始初始化测试数据...");

            // 清空旧数据（忽略不存在的表）
            truncateIfExists(stmt, "group_session");
            truncateIfExists(stmt, "group_rule");
            truncateIfExists(stmt, "flash_item");
            truncateIfExists(stmt, "flash_event");
            truncateIfExists(stmt, "order_item");
            truncateIfExists(stmt, "trade_order");
            truncateIfExists(stmt, "delivery_order");
            truncateIfExists(stmt, "product_sku");
            truncateIfExists(stmt, "product_spu");
            truncateIfExists(stmt, "user_address");
            truncateIfExists(stmt, "`user`");

            // 插入用户数据
            String userSql = """
                INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar_url`, `phone`, `status`) VALUES
                (1, 'wx_test_001', '张三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL1', '13800138001', 1),
                (2, 'wx_test_002', '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL2', '13800138002', 1),
                (3, 'wx_test_003', '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL3', '13800138003', 1),
                (4, 'wx_test_004', '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL4', '13800138004', 1),
                (5, 'wx_test_005', '钱七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL5', '13800138005', 1)
                """;
            stmt.execute(userSql);
            log.info("已插入5个测试用户");

            // 插入用户地址
            String addressSql = """
                INSERT INTO `user_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_addr`, `is_default`) VALUES
                (1, 1, '张三', '13800138001', '北京市', '北京市', '朝阳区', '大望路SOHO现代城A座1001室', 1),
                (2, 2, '李四', '13800138002', '广东省', '深圳市', '南山区', '科技园南区深圳湾科技生态园', 1),
                (3, 3, '王五', '13800138003', '浙江省', '杭州市', '西湖区', '文三路398号', 1)
                """;
            stmt.execute(addressSql);
            log.info("已插入3个用户地址");

            // 插入商品SPU
            String spuSql = """
                INSERT INTO `product_spu` (`id`, `name`, `category_id`, `main_image`, `detail_images`, `status`) VALUES
                (1, 'iPhone 15 Pro Max 256GB', 1, 'https://picsum.photos/400/400?random=1', '["https://picsum.photos/750/750?random=11","https://picsum.photos/750/750?random=12"]', 1),
                (2, '小米14 Ultra 512GB', 1, 'https://picsum.photos/400/400?random=2', '["https://picsum.photos/750/750?random=21","https://picsum.photos/750/750?random=22"]', 1),
                (3, 'iPad Pro 11寸 M4芯片', 1, 'https://picsum.photos/400/400?random=3', '["https://picsum.photos/750/750?random=31","https://picsum.photos/750/750?random=32"]', 1),
                (4, '索尼WH-1000XM5降噪耳机', 1, 'https://picsum.photos/400/400?random=4', '["https://picsum.photos/750/750?random=41","https://picsum.photos/750/750?random=42"]', 1),
                (5, '戴森吹风机 HD08', 1, 'https://picsum.photos/400/400?random=6', '["https://picsum.photos/750/750?random=61","https://picsum.photos/750/750?random=62"]', 1),
                (6, 'Tesla Model Y 1:18 精品模型车', 2, 'https://picsum.photos/400/400?random=8', '["https://picsum.photos/750/750?random=81","https://picsum.photos/750/750?random=82"]', 1),
                (7, 'Nike Air Max 270运动鞋', 3, 'https://picsum.photos/400/400?random=11', '["https://picsum.photos/750/750?random=111","https://picsum.photos/750/750?random=112"]', 1),
                (8, 'SK-II 神仙水 230ml', 4, 'https://picsum.photos/400/400?random=13', '["https://picsum.photos/750/750?random=131","https://picsum.photos/750/750?random=132"]', 1),
                (9, 'AirPods Pro 2', 1, 'https://picsum.photos/400/400?random=18', '["https://picsum.photos/750/750?random=181","https://picsum.photos/750/750?random=182"]', 1),
                (10, 'Apple Watch Series 9', 1, 'https://picsum.photos/400/400?random=19', '["https://picsum.photos/750/750?random=191","https://picsum.photos/750/750?random=192"]', 1)
                """;
            stmt.execute(spuSql);
            log.info("已插入10个商品SPU");

            // 插入商品SKU
            String skuSql = """
                INSERT INTO `product_sku` (`id`, `spu_id`, `specs`, `market_price`, `sale_price`, `stock`) VALUES
                (1, 1, '{"color": "原色钛金属", "storage": "256GB"}', 9999.00, 8999.00, 50),
                (2, 1, '{"color": "蓝色钛金属", "storage": "256GB"}', 9999.00, 8999.00, 30),
                (3, 2, '{"color": "黑色", "storage": "512GB"}', 6999.00, 5999.00, 100),
                (4, 3, '{"color": "深空灰", "storage": "256GB"}', 7999.00, 7199.00, 70),
                (5, 4, '{"color": "黑色"}', 2499.00, 1999.00, 85),
                (6, 5, '{"color": "紫色"}', 3690.00, 2990.00, 60),
                (7, 6, '{"color": "红色", "scale": "1:18"}', 399.00, 299.00, 200),
                (8, 7, '{"size": "42", "color": "黑白"}', 1299.00, 999.00, 120),
                (9, 8, '{"spec": "230ml"}', 1690.00, 1290.00, 90),
                (10, 9, '{"color": "白色"}', 1899.00, 1699.00, 150),
                (11, 10, '{"size": "45mm", "color": "午夜色"}', 3999.00, 3199.00, 80)
                """;
            stmt.execute(skuSql);
            log.info("已插入11个商品SKU");

            // 创建秒杀活动（今日三个时段）
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String eventSql = String.format("""
                INSERT INTO `flash_event` (`id`, `title`, `start_time`, `end_time`, `status`) VALUES
                (1, '早上8点场', '%s 08:00:00', '%s 12:00:00', 2),
                (2, '中午12点场', '%s 12:00:00', '%s 20:00:00', 1),
                (3, '晚上8点场', '%s 20:00:00', DATE_ADD('%s 20:00:00', INTERVAL 4 HOUR), 0)
                """, today, today, today, today, today, today);
            stmt.execute(eventSql);
            log.info("已插入3个秒杀活动");

            // 插入秒杀商品（12点场当前进行中）
            String flashSql = """
                INSERT INTO `flash_item` (`event_id`, `sku_id`, `flash_price`, `flash_stock`, `lock_stock`, `limit_per_user`) VALUES
                (2, 1, 6999.00, 20, 0, 1),
                (2, 3, 4999.00, 50, 0, 1),
                (2, 5, 1599.00, 35, 0, 1),
                (2, 6, 2199.00, 30, 0, 1),
                (2, 7, 199.00, 100, 0, 2),
                (2, 9, 890.00, 40, 0, 1),
                (2, 10, 1399.00, 60, 0, 2),
                (2, 11, 2799.00, 30, 0, 1)
                """;
            stmt.execute(flashSql);
            log.info("已插入8个秒杀商品");

            // 插入拼团规则
            String groupRuleSql = """
                INSERT INTO `group_rule` (`sku_id`, `group_price`, `member_count`, `duration_hours`, `status`) VALUES
                (7, 249.00, 2, 24, 1),
                (6, 2599.00, 3, 24, 1),
                (10, 1499.00, 2, 24, 1),
                (9, 1090.00, 3, 24, 1),
                (8, 899.00, 2, 24, 1),
                (5, 1799.00, 2, 24, 1)
                """;
            stmt.execute(groupRuleSql);
            log.info("已插入6个拼团规则");

            // 插入拼团会话
            String sessionSql = """
                INSERT INTO `group_session` (`id`, `rule_id`, `initiator_id`, `status`, `current_count`, `expire_time`) VALUES
                (1, 1, 1, 0, 1, DATE_ADD(NOW(), INTERVAL 12 HOUR)),
                (2, 2, 1, 0, 2, DATE_ADD(NOW(), INTERVAL 18 HOUR)),
                (3, 3, 2, 0, 1, DATE_ADD(NOW(), INTERVAL 20 HOUR)),
                (4, 6, 1, 1, 2, DATE_ADD(NOW(), INTERVAL -2 HOUR))
                """;
            stmt.execute(sessionSql);
            log.info("已插入4个拼团会话");

            log.info("✅ 测试数据初始化完成！");

            return Result.ok("测试数据初始化成功！已插入：5个用户、10个商品、8个秒杀商品、6个拼团规则");

        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
            return Result.error("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 辅助方法：安全地清空表（如果表存在）
     */
    private void truncateIfExists(Statement stmt, String tableName) {
        try {
            stmt.execute("TRUNCATE TABLE " + tableName);
        } catch (Exception e) {
            // 表不存在或其他错误，忽略
            log.debug("Table {} does not exist or cannot be truncated: {}", tableName, e.getMessage());
        }
    }
}
