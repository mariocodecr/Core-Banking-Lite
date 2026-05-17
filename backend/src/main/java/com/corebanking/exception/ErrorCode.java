package com.corebanking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Generic
    INTERNAL_SERVER_ERROR("CBL-001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("CBL-002", "Validation failed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("CBL-003", "Resource not found", HttpStatus.NOT_FOUND),

    // Authentication & Authorization
    UNAUTHORIZED("CBL-010", "Authentication required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("CBL-011", "Access forbidden", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("CBL-012", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("CBL-013", "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("CBL-014", "Token is invalid", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("CBL-015", "Account is locked", HttpStatus.FORBIDDEN),

    // Business rules
    BUSINESS_RULE_VIOLATION("CBL-020", "Business rule violation", HttpStatus.UNPROCESSABLE_ENTITY),
    DUPLICATE_RESOURCE("CBL-021", "Resource already exists", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("CBL-022", "Insufficient balance", HttpStatus.UNPROCESSABLE_ENTITY),
    ACCOUNT_INACTIVE("CBL-023", "Account is not active", HttpStatus.UNPROCESSABLE_ENTITY),
    DAILY_LIMIT_EXCEEDED("CBL-024", "Daily transfer limit exceeded", HttpStatus.UNPROCESSABLE_ENTITY),

    // Concurrency
    CONCURRENCY_CONFLICT("CBL-030", "Concurrent modification detected, please retry", HttpStatus.CONFLICT),

    // Rate limiting
    RATE_LIMIT_EXCEEDED("CBL-040", "Too many requests, please slow down", HttpStatus.TOO_MANY_REQUESTS),

    // Investments
    INSTRUMENT_NOT_FOUND("CBL-050", "Instrument not found or not available", HttpStatus.NOT_FOUND),
    INSUFFICIENT_SHARES("CBL-051", "Insufficient shares for this operation", HttpStatus.UNPROCESSABLE_ENTITY),
    MARKET_DATA_UNAVAILABLE("CBL-052", "Market data unavailable and no fallback price in DB", HttpStatus.SERVICE_UNAVAILABLE),
    ACCOUNT_CURRENCY_NOT_USD("CBL-053", "Investment accounts must be denominated in USD", HttpStatus.UNPROCESSABLE_ENTITY);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
