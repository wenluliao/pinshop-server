package com.flashbuy.infrastructure.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex Configuration
 */
@Configuration
public class MyBatisFlexConfig {

    @Bean
    public MyBatisFlexCustomizer myBatisFlexCustomizer() {
        return configuration -> {
            // MyBatis-Flex handles most configuration automatically
            // Just add any custom settings here if needed
        };
    }
}
