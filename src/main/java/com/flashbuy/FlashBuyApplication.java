package com.flashbuy;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FlashGroupBuy Application
 * High-performance flash sale and group buying platform
 * Powered by Java 25 Virtual Threads + Spring Boot 3.4 + GraalVM Native Image
 *
 * @author FlashBuy Team
 */
@SpringBootApplication(
    exclude = {
        org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration.class
    }
)
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan({
    "com.flashbuy.domain.item.mapper",
    "com.flashbuy.domain.user.mapper",
    "com.flashbuy.domain.trade.mapper",
    "com.flashbuy.domain.marketing.mapper"
})
public class FlashBuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashBuyApplication.class, args);
    }
}
