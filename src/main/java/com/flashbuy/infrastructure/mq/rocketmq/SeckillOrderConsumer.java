package com.flashbuy.infrastructure.mq.rocketmq;

import com.flashbuy.application.seckill.TradeOrderMessage;
import com.flashbuy.domain.item.entity.FlashItem;
import com.flashbuy.domain.item.mapper.FlashItemMapper;
import com.flashbuy.domain.trade.entity.TradeOrder;
import com.flashbuy.domain.trade.mapper.TradeOrderMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RocketMQ Consumer for Seckill Orders
 * Async order creation to decouple from inventory deduction
 */
// Temporarily disabled - RocketMQ not available in test environment
// @Component
// @RocketMQMessageListener(
//         topic = "seckill-order-topic",
//         consumerGroup = "seckill-order-consumer-group"
// )
public class SeckillOrderConsumer implements RocketMQListener<TradeOrderMessage> {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderConsumer.class);

    private final TradeOrderMapper tradeOrderMapper;
    private final FlashItemMapper flashItemMapper;

    // Simple ID generator (in production, use Snowflake algorithm)
    private final AtomicLong idGenerator = new AtomicLong(10000);

    public SeckillOrderConsumer(
            TradeOrderMapper tradeOrderMapper,
            FlashItemMapper flashItemMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.flashItemMapper = flashItemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(TradeOrderMessage message) {
        log.info("Received seckill order message: {}", message);

        try {
            // Step 1: Get flash item info
            FlashItem flashItem = flashItemMapper.selectOneByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .where(FlashItem::getSkuId).eq(message.skuId())
                            .and(FlashItem::getEventId).eq(message.eventId())
                            .limit(1)
            );

            if (flashItem == null) {
                log.error("Flash item not found: skuId={}", message.skuId());
                return;
            }

            // Step 2: Create order
            TradeOrder order = new TradeOrder();
            order.setId(idGenerator.incrementAndGet());
            order.setUserId(message.userId());
            order.setTotalAmount(flashItem.getFlashPrice().multiply(BigDecimal.valueOf(message.count())));
            order.setPayAmount(flashItem.getFlashPrice().multiply(BigDecimal.valueOf(message.count())));
            order.setStatus(10); // 10: Unpaid
            order.setOrderType("FLASH");
            order.setMarketingId(message.eventId());
            order.setReceiverInfo("{\"name\":\"Test User\",\"phone\":\"13800138000\",\"address\":\"Test Address\"}");
            order.setCreateTime(LocalDateTime.now());
            order.setExtraJson("{\"skuId\":" + message.skuId() + ",\"count\":" + message.count() + "}");

            tradeOrderMapper.insert(order);

            // Step 3: Update locked stock in DB
            // In production, use optimistic lock to prevent race condition
            // UPDATE flash_item SET lock_stock = lock_stock + 1 WHERE id = ? AND lock_stock + 1 <= flash_stock

            log.info("Order created successfully: orderId={}, userId={}", order.getId(), message.userId());

            // Step 4: Write order status to Redis for frontend polling
            // TODO: Implement Redis cache for order status

        } catch (Exception e) {
            log.error("Failed to process seckill order: {}", message, e);
            throw e; // Trigger retry
        }
    }
}
