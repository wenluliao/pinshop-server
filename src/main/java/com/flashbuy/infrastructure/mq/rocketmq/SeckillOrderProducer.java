package com.flashbuy.infrastructure.mq.rocketmq;

import com.flashbuy.application.seckill.TradeOrderMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * RocketMQ Producer for Seckill Orders
 * Async message sending to decouple order creation from inventory deduction
 */
// Temporarily disabled - RocketMQ not available in test environment
// @Component
public class SeckillOrderProducer {

    private final RocketMQTemplate rocketMQTemplate;

    private static final String TOPIC = "seckill-order-topic";

    public SeckillOrderProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * Send order creation message
     * Message will be consumed asynchronously
     */
    public boolean sendOrderMessage(TradeOrderMessage message) {
        try {
            Message<TradeOrderMessage> msg = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.syncSend(TOPIC, msg);
            return true;
        } catch (Exception e) {
            // Log error but don't throw exception (performance priority)
            System.err.println("Failed to send MQ message: " + e.getMessage());
            return false;
        }
    }
}
