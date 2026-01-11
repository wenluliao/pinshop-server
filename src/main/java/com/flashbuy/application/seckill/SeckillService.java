package com.flashbuy.application.seckill;

import com.flashbuy.domain.item.entity.FlashItem;
import com.flashbuy.domain.item.mapper.FlashItemMapper;
import com.flashbuy.infrastructure.cache.LocalStockCache;
import com.flashbuy.infrastructure.cache.StockLuaScript;
import com.flashbuy.infrastructure.mq.rocketmq.SeckillOrderProducer;
import com.flashbuy.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Flash Sale Core Service
 * Implements high-concurrency flash sale logic
 *
 * Core Flow:
 * 1. Local cache check (Caffeine) - block 90% requests when stock empty
 * 2. Redis atomic deduction (Lua script) - thread-safe stock deduction
 * 3. MQ async order creation - prevent DB pressure
 * 4. Return immediately - frontend polls for result
 */
@Service
public class SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillService.class);

    private final LocalStockCache localStockCache;
    private final StockLuaScript stockLuaScript;
    private final SeckillOrderProducer mqProducer;
    private final FlashItemMapper flashItemMapper;

    private static final String STOCK_PREFIX = "flash:stock:";
    private static final String USER_PREFIX = "flash:user:";

    @Autowired
    public SeckillService(
            LocalStockCache localStockCache,
            StockLuaScript stockLuaScript,
            @Nullable SeckillOrderProducer mqProducer,
            FlashItemMapper flashItemMapper) {
        this.localStockCache = localStockCache;
        this.stockLuaScript = stockLuaScript;
        this.mqProducer = mqProducer;
        this.flashItemMapper = flashItemMapper;
    }

    /**
     * Execute flash sale
     * This method must complete within 50ms to handle 100K+ QPS
     */
    public SeckillResponse execute(SeckillRequest request) {
        long startTime = System.currentTimeMillis();

        // Step 1: Local cache check (Level 1 defense)
        if (localStockCache.isEmpty(request.skuId().toString())) {
            log.warn("Local cache: stock empty for skuId={}", request.skuId());
            throw new BusinessException("Flash sale ended");
        }

        // Step 2: Build Redis keys
        String stockKey = STOCK_PREFIX + request.skuId();
        String userLimitKey = USER_PREFIX + request.eventId() + ":" + request.skuId();

        // Step 3: Redis atomic deduction with Lua script (Level 2 defense)
        Long result = stockLuaScript.deductStock(
                stockKey,
                userLimitKey,
                request.userId().toString(),
                request.count()
        );

        // Step 4: Handle result
        if (result == null || result < 0) {
            if (result == -1) {
                // Insufficient stock - mark as empty in local cache
                localStockCache.markEmpty(request.skuId().toString());
                throw new BusinessException("Insufficient stock");
            } else if (result == -2) {
                throw new BusinessException("You have already purchased this item");
            }
            throw new BusinessException("Flash sale failed");
        }

        // Step 5: Send MQ message for async order creation (if MQ is available)
        if (mqProducer != null) {
            TradeOrderMessage mqMessage = new TradeOrderMessage(
                    request.userId(),
                    request.eventId(),
                    request.skuId(),
                    request.count(),
                    System.currentTimeMillis()
            );

            boolean sent = mqProducer.sendOrderMessage(mqMessage);
            if (!sent) {
                // Rollback stock if MQ fails
                stockLuaScript.recoverStock(stockKey, request.count());
                throw new BusinessException("System busy, please try again");
            }
        } else {
            log.warn("MQ producer not available, seckill executed without async order creation");
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Seckill executed successfully: userId={}, skuId={}, duration={}ms",
                request.userId(), request.skuId(), duration);

        // Step 6: Return immediately (frontend polls for order status)
        return SeckillResponse.queuing();
    }

    /**
     * Warm-up: Preload stock to Redis before event starts
     * Called by scheduled job
     */
    public void warmUpStock(Long skuId, Integer stock) {
        String stockKey = STOCK_PREFIX + skuId;
        stockLuaScript.initStock(stockKey, stock);
        localStockCache.clearEmpty(skuId.toString());
        log.info("Stock warmed up: skuId={}, stock={}", skuId, stock);
    }
}
