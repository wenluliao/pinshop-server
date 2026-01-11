package com.flashbuy.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Result wrapper
 */
class ResultTest {

    @Test
    void testOkWithData() {
        String data = "Test Data";
        Result<String> result = Result.ok(data);

        assertEquals(200, result.code());
        assertEquals("Success", result.message());
        assertEquals(data, result.data());
        assertTrue(result.timestamp() > 0);
    }

    @Test
    void testOkWithoutData() {
        Result<Void> result = Result.ok();

        assertEquals(200, result.code());
        assertEquals("Success", result.message());
        assertNull(result.data());
    }

    @Test
    void testError() {
        String errorMsg = "System error";
        Result<Void> result = Result.error(errorMsg);

        assertEquals(500, result.code());
        assertEquals(errorMsg, result.message());
        assertNull(result.data());
    }

    @Test
    void testBusinessError() {
        String errorMsg = "Business validation failed";
        Result<Void> result = Result.businessError(errorMsg);

        assertEquals(400, result.code());
        assertEquals(errorMsg, result.message());
        assertNull(result.data());
    }

    @Test
    void testErrorWithCode() {
        int customCode = 404;
        String errorMsg = "Not found";
        Result<Void> result = Result.error(customCode, errorMsg);

        assertEquals(customCode, result.code());
        assertEquals(errorMsg, result.message());
        assertNull(result.data());
    }
}
