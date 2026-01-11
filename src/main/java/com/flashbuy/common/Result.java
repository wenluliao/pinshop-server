package com.flashbuy.common;

/**
 * Unified API Response Wrapper
 * All API responses use this structure
 *
 * @param <T> Response data type
 */
public record Result<T>(
        int code,
        String message,
        T data,
        long timestamp
) {

    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 500;
    private static final int BUSINESS_ERROR_CODE = 400;

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS_CODE, "Success", data, System.currentTimeMillis());
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> businessError(String message) {
        return new Result<>(BUSINESS_ERROR_CODE, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> success() {
        return ok(null);
    }

    public static <T> Result<T> success(T data) {
        return ok(data);
    }
}
