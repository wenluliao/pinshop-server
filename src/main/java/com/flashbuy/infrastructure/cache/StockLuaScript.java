package com.flashbuy.infrastructure.cache;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Redis Lua Script Service for Stock Deduction
 * Pre-loads Lua script to avoid compilation on each request
 */
@Service
public class StockLuaScript {

    private final DefaultRedisScript<Long> deductScript;
    private final RedisTemplate<String, Object> redisTemplate;

    public StockLuaScript(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // Pre-load Lua script
        this.deductScript = new DefaultRedisScript<>();
        this.deductScript.setLocation(new ClassPathResource("lua/deduct_stock.lua"));
        this.deductScript.setResultType(Long.class);
    }

    /**
     * Execute stock deduction with Lua script (atomic operation)
     *
     * @param stockKey    Redis key for stock, e.g. "flash:stock:123"
     * @param userLimitKey Redis key for user limit, e.g. "flash:user:1:123"
     * @param userId      User ID
     * @param count       Quantity to deduct (default 1)
     * @return Remaining stock if success, -1 if insufficient stock, -2 if user already bought
     */
    public Long deductStock(String stockKey, String userLimitKey, String userId, int count) {
        return redisTemplate.execute(
                deductScript,
                Collections.singletonList(stockKey),
                userId,
                String.valueOf(count)
        );
    }

    /**
     * Initialize stock in Redis
     * Called during system warm-up before flash sale starts
     */
    public void initStock(String stockKey, int stock) {
        redisTemplate.opsForValue().set(stockKey, stock);
    }

    /**
     * Check if stock is empty (for local cache)
     */
    public int getStock(String stockKey) {
        Object value = redisTemplate.opsForValue().get(stockKey);
        return value != null ? (Integer) value : 0;
    }

    /**
     * Recover stock (for rollback scenarios)
     */
    public void recoverStock(String stockKey, int count) {
        redisTemplate.opsForValue().increment(stockKey, count);
    }
}
