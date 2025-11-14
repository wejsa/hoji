package com.hoji.common.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * 공통 API 응답 구조
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T? = null, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                code = ResultCode.SUCCESS.code,
                message = message,
                data = data
            )
        }

        fun <T> success(code: ResultCode, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                code = code.code,
                message = code.message,
                data = data
            )
        }

        fun <T> error(code: ResultCode, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                code = code.code,
                message = message ?: code.message,
                data = null
            )
        }

        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                code = code,
                message = message,
                data = null
            )
        }
    }
}
