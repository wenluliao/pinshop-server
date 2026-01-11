package com.flashbuy.application.seckill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Seckill Request/Response DTOs
 * Validates Record structure and business logic
 */
class SeckillServiceTest {

    @Test
    void testSeckillRequest() {
        SeckillRequest request = new SeckillRequest(1L, 100L, 2, 10001L);

        assertEquals(1L, request.eventId());
        assertEquals(100L, request.skuId());
        assertEquals(2, request.count());
        assertEquals(10001L, request.userId());
    }

    @Test
    void testSeckillRequestWithDefaultCount() {
        SeckillRequest request = new SeckillRequest(1L, 100L, null, 10001L);

        // Count should default to 1 when null is passed
        assertEquals(1, request.count());
    }

    @Test
    void testSeckillResponseQueuing() {
        SeckillResponse response = SeckillResponse.queuing();

        assertEquals(0, response.status());
        assertEquals("Order processing, please wait", response.message());
        assertNull(response.orderId());
        assertNotNull(response.timestamp());
    }

    @Test
    void testSeckillResponseSuccess() {
        Long orderId = 12345L;
        SeckillResponse response = SeckillResponse.success(orderId);

        assertEquals(1, response.status());
        assertEquals("Order created successfully", response.message());
        assertEquals(orderId, response.orderId());
        assertNotNull(response.timestamp());
    }

    @Test
    void testSeckillResponseFailed() {
        SeckillResponse response = SeckillResponse.failed("Insufficient stock");

        assertEquals(-1, response.status());
        assertEquals("Insufficient stock", response.message());
        assertNull(response.orderId());
        assertNotNull(response.timestamp());
    }

    @Test
    void testTradeOrderMessage() {
        TradeOrderMessage message = new TradeOrderMessage(10001L, 1L, 100L, 2, System.currentTimeMillis());

        assertEquals(10001L, message.userId());
        assertEquals(1L, message.eventId());
        assertEquals(100L, message.skuId());
        assertEquals(2, message.count());
        assertNotNull(message.timestamp());
    }
}
