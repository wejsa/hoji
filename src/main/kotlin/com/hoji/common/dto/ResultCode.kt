package com.hoji.common.dto

/**
 * 공통 응답 코드
 */
enum class ResultCode(
    val code: String,
    val message: String
) {
    // Success
    SUCCESS("0000", "Success"),

    // Client Errors (4xxx)
    BAD_REQUEST("4000", "Bad Request"),
    UNAUTHORIZED("4001", "Unauthorized"),
    TOKEN_EXPIRED("4002", "Token Expired"),
    FORBIDDEN("4003", "Forbidden"),
    INVALID_TOKEN("4007", "Invalid Token"),
    NOT_FOUND("4004", "Not Found"),
    METHOD_NOT_ALLOWED("4005", "Method Not Allowed"),
    CONFLICT("4009", "Conflict"),
    VALIDATION_ERROR("4010", "Validation Error"),
    INVALID_INPUT("4011", "Invalid Input"),

    // Server Errors (5xxx)
    INTERNAL_SERVER_ERROR("5000", "Internal Server Error"),
    SERVICE_UNAVAILABLE("5003", "Service Unavailable"),
    DATABASE_ERROR("5010", "Database Error"),
    EXTERNAL_API_ERROR("5020", "External API Error"),

    // Custom Business Errors (6xxx)
    BUSINESS_ERROR("6000", "Business Error"),
}
