package com.flashbuy.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Local stock status cache using Caffeine
 * First level defense: block 90% of requests when stock is empty
 * Avoids Redis network overhead
 */
@Component
public class LocalStockCache {

    private final Cache<String, Boolean> cache;

    public LocalStockCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Mark stock as empty (no need to check Redis)
     */
    public void markEmpty(String skuId) {
        String key = "stock_empty_" + skuId;
        cache.put(key, true);
    }

    /**
     * Check if stock is marked as empty
     */
    public boolean isEmpty(String skuId) {
        String key = "stock_empty_" + skuId;
        return cache.getIfPresent(key) != null;
    }

    /**
     * Clear empty mark (when stock is replenished)
     */
    public void clearEmpty(String skuId) {
        String key = "stock_empty_" + skuId;
        cache.invalidate(key);
    }
}
