package com.flashbuy.application.seckill;

/**
 * Trade Order Message for MQ
 * Record type for Native Image optimization
 */
public record TradeOrderMessage(
        Long userId,
        Long eventId,
        Long skuId,
        Integer count,
        Long timestamp
) {
    public TradeOrderMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}
