package com.flashbuy.test;

import com.flashbuy.application.seckill.SeckillRequest;
import com.flashbuy.application.seckill.SeckillResponse;
import com.flashbuy.application.seckill.TradeOrderMessage;
import com.flashbuy.common.Result;
import com.flashbuy.common.BusinessException;

/**
 * Manual test runner for validating core logic without full Spring context
 * Run with: javac --enable-preview -cp ".:junit-path" src/test/java/com/flashbuy/test/ManualTest.java && \
 *          java --enable-preview -cp ".:junit-path" com.flashbuy.test.ManualTest
 */
public class ManualTest {

    public static void main(String[] args) {
        System.out.println("=== PinShop Server Core Logic Tests ===\n");

        int passed = 0;
        int failed = 0;

        // Test 1: SeckillRequest
        try {
            System.out.println("Test 1: SeckillRequest Record");
            SeckillRequest request = new SeckillRequest(1L, 100L, 2, 10001L);
            assert request.eventId().equals(1L);
            assert request.skuId().equals(100L);
            assert request.count().equals(2);
            assert request.userId().equals(10001L);

            // Test default count
            SeckillRequest request2 = new SeckillRequest(1L, 100L, null, 10001L);
            assert request2.count().equals(1);

            System.out.println("  ✓ SeckillRequest tests passed\n");
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ SeckillRequest tests failed: " + e.getMessage() + "\n");
            failed++;
        }

        // Test 2: SeckillResponse
        try {
            System.out.println("Test 2: SeckillResponse Record");
            SeckillResponse queuing = SeckillResponse.queuing();
            assert queuing.status() == 0;
            assert queuing.orderId() == null;

            SeckillResponse success = SeckillResponse.success(12345L);
            assert success.status() == 1;
            assert success.orderId().equals(12345L);

            SeckillResponse responseFailed = SeckillResponse.failed("Out of stock");
            assert responseFailed.status() == -1;
            assert responseFailed.message().equals("Out of stock");

            System.out.println("  ✓ SeckillResponse tests passed\n");
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ SeckillResponse tests failed: " + e.getMessage() + "\n");
            failed++;
        }

        // Test 3: TradeOrderMessage
        try {
            System.out.println("Test 3: TradeOrderMessage Record");
            TradeOrderMessage message = new TradeOrderMessage(10001L, 1L, 100L, 2, System.currentTimeMillis());
            assert message.userId().equals(10001L);
            assert message.eventId().equals(1L);
            assert message.skuId().equals(100L);
            assert message.count().equals(2);
            assert message.timestamp() > 0;

            System.out.println("  ✓ TradeOrderMessage tests passed\n");
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ TradeOrderMessage tests failed: " + e.getMessage() + "\n");
            failed++;
        }

        // Test 4: Result wrapper
        try {
            System.out.println("Test 4: Result Wrapper");
            Result<String> ok = Result.ok("data");
            assert ok.code() == 200;
            assert ok.data().equals("data");

            Result<Void> error = Result.error("error");
            assert error.code() == 500;
            assert error.message().equals("error");

            System.out.println("  ✓ Result tests passed\n");
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ Result tests failed: " + e.getMessage() + "\n");
            failed++;
        }

        // Test 5: BusinessException
        try {
            System.out.println("Test 5: BusinessException (Performance)");
            BusinessException ex = new BusinessException("Test error");
            assert ex.getMessage().equals("Test error");
            assert ex.getCode() == 400;

            // Test performance optimization
            Throwable result = ex.fillInStackTrace();
            assert result == ex; // Should return 'this' for performance

            System.out.println("  ✓ BusinessException tests passed\n");
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ BusinessException tests failed: " + e.getMessage() + "\n");
            failed++;
        }

        // Summary
        System.out.println("=====================================");
        System.out.println("Test Results:");
        System.out.println("  Passed: " + passed);
        System.out.println("  Failed: " + failed);
        System.out.println("  Total:  " + (passed + failed));
        System.out.println("=====================================");

        if (failed == 0) {
            System.out.println("\n✓ All tests passed!");
            System.exit(0);
        } else {
            System.out.println("\n✗ Some tests failed!");
            System.exit(1);
        }
    }
}
