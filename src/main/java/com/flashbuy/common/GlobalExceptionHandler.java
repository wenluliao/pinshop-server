package com.flashbuy.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeoutException;

/**
 * Global Exception Handler
 * Centralized exception handling with performance optimization
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(Exception e) {
        String message = "Validation failed";
        if (e instanceof MethodArgumentNotValidException validException) {
            if (validException.getBindingResult().hasErrors() &&
                    validException.getBindingResult().getFieldError() != null) {
                message = validException.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        log.warn("Validation exception: {}", message);
        return Result.businessError(message);
    }

    @ExceptionHandler(TimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public Result<?> handleTimeoutException(TimeoutException e) {
        log.error("Timeout exception: {}", e.getMessage());
        return Result.error(408, "Request timeout, please try again");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("System exception", e);
        return Result.error("System error, please try again later");
    }
}
