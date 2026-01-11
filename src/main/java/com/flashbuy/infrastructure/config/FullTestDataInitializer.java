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
 * 完整测试数据初始化器
 * 包含用户、商品、订单等完整测试数据
 */
@Component
@Order(2)
@ConditionalOnBean(DataSource.class)
public class FullTestDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FullTestDataInitializer.class);

    private final DataSource dataSource;

    public FullTestDataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            log.info("开始初始化完整测试数据...");

            // 检查是否已有用户数据
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM `user`");
            if (rs.next() && rs.getInt(1) >= 10) {
                log.info("数据库已有完整测试数据，跳过初始化");
                return;
            }

            // 插入用户数据
            String userSql = """
                INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar_url`, `phone`, `status`) VALUES
                (1, 'wx_test_001', '张三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL1', '13800138001', 1),
                (2, 'wx_test_002', '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL2', '13800138002', 1),
                (3, 'wx_test_003', '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL3', '13800138003', 1),
                (4, 'wx_test_004', '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL4', '13800138004', 1),
                (5, 'wx_test_005', '钱七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL5', '13800138005', 1),
                (6, 'wx_test_006', '孙八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL6', '13800138006', 1),
                (7, 'wx_test_007', '周九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL7', '13800138007', 1),
                (8, 'wx_test_008', '吴十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL8', '13800138008', 1),
                (9, 'wx_test_009', '郑十一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL9', '13800138009', 1),
                (10, 'wx_test_010', '王十二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTL10', '13800138010', 1)
                ON DUPLICATE KEY UPDATE nickname=VALUES(nickname)
                """;
            stmt.execute(userSql);
            log.info("已插入10个测试用户");

            // 插入用户地址
            String addressSql = """
                INSERT INTO `user_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_addr`, `is_default`) VALUES
                (1, 1, '张三', '13800138001', '北京市', '北京市', '朝阳区', '大望路SOHO现代城A座1001室', 1),
                (2, 1, '张三', '13800138001', '北京市', '北京市', '海淀区', '中关村大街1号科技大厦8层', 0),
                (3, 1, '张三', '13800138001', '上海市', '上海市', '浦东新区', '世纪大道100号', 0),
                (4, 2, '李四', '13800138002', '广东省', '深圳市', '南山区', '科技园南区深圳湾科技生态园', 1),
                (5, 2, '李四', '13800138002', '广东省', '深圳市', '福田区', '福田街道福华路168号', 0),
                (6, 3, '王五', '13800138003', '浙江省', '杭州市', '西湖区', '文三路398号', 1),
                (7, 4, '赵六', '13800138004', '江苏省', '南京市', '玄武区', '新街口商圈中山路18号', 1),
                (8, 5, '钱七', '13800138005', '四川省', '成都市', '武侯区', '天府软件园D区', 1),
                (9, 6, '孙八', '13800138006', '湖北省', '武汉市', '江汉区', '中山大道538号', 1),
                (10, 7, '周九', '13800138007', '陕西省', '西安市', '雁塔区', '高新路25号', 1)
                ON DUPLICATE KEY UPDATE detail_addr=VALUES(detail_addr)
                """;
            stmt.execute(addressSql);
            log.info("已插入10个用户地址");

            // 插入拼团会话
            String sessionSql = """
                INSERT INTO `group_session` (`id`, `rule_id`, `initiator_id`, `status`, `current_count`, `expire_time`) VALUES
                (1, 1, 1, 0, 1, DATE_ADD(NOW(), INTERVAL 12 HOUR)),
                (2, 2, 1, 0, 2, DATE_ADD(NOW(), INTERVAL 18 HOUR)),
                (3, 3, 2, 0, 1, DATE_ADD(NOW(), INTERVAL 20 HOUR)),
                (4, 4, 3, 0, 2, DATE_ADD(NOW(), INTERVAL 22 HOUR)),
                (5, 5, 4, 0, 1, DATE_ADD(NOW(), INTERVAL 10 HOUR)),
                (6, 6, 1, 1, 2, DATE_ADD(NOW(), INTERVAL -2 HOUR)),
                (7, 7, 2, 1, 2, DATE_ADD(NOW(), INTERVAL -5 HOUR))
                ON DUPLICATE KEY UPDATE status=VALUES(status)
                """;
            stmt.execute(sessionSql);
            log.info("已插入7个拼团会话");

            // 插入订单数据
            String orderSql = """
                INSERT INTO `trade_order` (`id`, `user_id`, `total_amount`, `pay_amount`, `status`, `order_type`, `marketing_id`, `receiver_info`, `create_time`, `pay_time`) VALUES
                (10001, 1, 6999.00, 6999.00, 10, 'FLASH', 1, '{"name":"张三","phone":"13800138001","address":"北京市朝阳区大望路SOHO现代城A座1001室"}', DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL),
                (10002, 2, 1499.00, 1499.00, 10, 'GROUP', 3, '{"name":"李四","phone":"13800138002","address":"广东省深圳市南山区科技园南区深圳湾科技生态园"}', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL),
                (10003, 1, 890.00, 890.00, 20, 'FLASH', 6, '{"name":"张三","phone":"13800138001","address":"北京市朝阳区大望路SOHO现代城A座1001室"}', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR 50 MINUTE)),
                (10004, 3, 1799.00, 1799.00, 20, 'GROUP', 6, '{"name":"王五","phone":"13800138003","address":"浙江省杭州市西湖区文三路398号"}', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR 50 MINUTE)),
                (10005, 2, 1399.00, 1399.00, 30, 'GROUP', 7, '{"name":"李四","phone":"13800138002","address":"广东省深圳市南山区科技园南区深圳湾科技生态园"}', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 23 HOUR 50 MINUTE)),
                (10006, 4, 249.00, 249.00, 40, 'GROUP', 6, '{"name":"赵六","phone":"13800138004","address":"江苏省南京市玄武区新街口商圈中山路18号"}', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY 23 HOUR 50 MINUTE))
                ON DUPLICATE KEY UPDATE status=VALUES(status)
                """;
            stmt.execute(orderSql);
            log.info("已插入6个测试订单");

            // 插入订单商品
            String itemSql = """
                INSERT INTO `order_item` (`order_id`, `sku_id`, `sku_name`, `price`, `quantity`) VALUES
                (10001, 1, 'iPhone 15 Pro Max 原色钛金属 256GB', 6999.00, 1),
                (10002, 7, 'AirPods Pro 2 白色', 1499.00, 1),
                (10003, 6, '戴森吹风机 HD08 紫色', 890.00, 1),
                (10004, 11, '索尼WH-1000XM5降噪耳机 黑色', 1799.00, 1),
                (10005, 23, '兰蔻小黑瓶精华 50ml', 1399.00, 1),
                (10006, 5, 'Tesla Model Y 1:18 精品模型车 红色', 249.00, 1)
                ON DUPLICATE KEY UPDATE price=VALUES(price)
                """;
            stmt.execute(itemSql);
            log.info("已插入6个订单商品明细");

            log.info("✅ 完整测试数据初始化完成！");
            log.info("📊 数据统计：");
            log.info("  - 10个测试用户");
            log.info("  - 10个用户地址");
            log.info("  - 10个商品SPU");
            log.info("  - 12个商品SKU");
            log.info("  - 3个秒杀活动");
            log.info("  - 12个秒杀商品");
            log.info("  - 6个拼团规则");
            log.info("  - 7个拼团会话");
            log.info("  - 6个测试订单");

        } catch (Exception e) {
            log.error("初始化完整测试数据失败", e);
            // 不抛出异常，允许应用继续运行
        }
    }
}
