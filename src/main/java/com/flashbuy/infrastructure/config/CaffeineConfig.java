package com.flashbuy.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Local Cache Configuration
 * First level cache to reduce Redis network overhead
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                // Maximum cache size
                .maximumSize(10000)
                // Expire after write
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // Record cache stats
                .recordStats());

        return cacheManager;
    }
}
