package com.flashbuy.common;

/**
 * Business Exception
 * Optimized for performance: no stack trace filling
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }

    public int getCode() {
        return code;
    }

    @Override
    public Throwable fillInStackTrace() {
        // Override to avoid expensive stack trace generation
        return this;
    }
}
