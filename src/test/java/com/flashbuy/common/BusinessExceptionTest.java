package com.flashbuy.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessException
 * Validates performance optimization (no stack trace filling)
 */
class BusinessExceptionTest {

    @Test
    void testBusinessExceptionWithMessage() {
        String message = "Business rule violation";
        BusinessException exception = new BusinessException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void testBusinessExceptionWithCodeAndMessage() {
        int code = 409;
        String message = "Conflict";
        BusinessException exception = new BusinessException(code, message);

        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testBusinessExceptionFillInStackTrace() {
        BusinessException exception = new BusinessException("Test");

        // fillInStackTrace should return 'this' for performance
        Throwable result = exception.fillInStackTrace();

        assertSame(exception, result);
    }

    @Test
    void testBusinessExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        BusinessException exception = new BusinessException("Wrapper message", cause);

        assertEquals("Wrapper message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(400, exception.getCode());
    }
}
