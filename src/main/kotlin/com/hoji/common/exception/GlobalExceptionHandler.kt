package com.hoji.common.exception

import com.hoji.common.dto.ApiResponse
import com.hoji.common.dto.ResultCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

private val logger = KotlinLogging.logger {}

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "BusinessException: ${e.message}" }
        val response = ApiResponse.error<Unit>(e.resultCode, e.message)
        return ResponseEntity.status(getHttpStatus(e.resultCode)).body(response)
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String?>>> {
        logger.warn { "Validation error: ${e.bindingResult}" }
        val errors = e.bindingResult.fieldErrors.associate {
            it.field to it.defaultMessage
        }
        val response = ApiResponse.error<Map<String, String?>>(
            ResultCode.VALIDATION_ERROR,
            "Validation failed: ${errors.entries.joinToString { "${it.key}: ${it.value}" }}"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * BindException 처리
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Bind error: ${e.bindingResult}" }
        val errors = e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        val response = ApiResponse.error<Unit>(ResultCode.VALIDATION_ERROR, "Validation failed: $errors")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Missing Parameter 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Missing parameter: ${e.parameterName}" }
        val response = ApiResponse.error<Unit>(
            ResultCode.BAD_REQUEST,
            "Required parameter '${e.parameterName}' is missing"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Type Mismatch 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Type mismatch: ${e.name}" }
        val response = ApiResponse.error<Unit>(
            ResultCode.INVALID_INPUT,
            "Invalid value for parameter '${e.name}'"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * HttpMessageNotReadable 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Message not readable: ${e.message}" }
        val response = ApiResponse.error<Unit>(ResultCode.BAD_REQUEST, "Malformed JSON request")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Method Not Supported 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Method not supported: ${e.method}" }
        val response = ApiResponse.error<Unit>(
            ResultCode.METHOD_NOT_ALLOWED,
            "Method '${e.method}' is not supported"
        )
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    /**
     * NoHandlerFound 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(e: NoHandlerFoundException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "No handler found: ${e.requestURL}" }
        val response = ApiResponse.error<Unit>(ResultCode.NOT_FOUND, "Endpoint not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error(e) { "Unexpected error: ${e.message}" }
        val response = ApiResponse.error<Unit>(ResultCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    /**
     * ResultCode에 따른 HTTP Status 매핑
     */
    private fun getHttpStatus(resultCode: ResultCode): HttpStatus {
        return when (resultCode.code.substring(0, 1)) {
            "4" -> when (resultCode) {
                ResultCode.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
                ResultCode.FORBIDDEN -> HttpStatus.FORBIDDEN
                ResultCode.NOT_FOUND -> HttpStatus.NOT_FOUND
                ResultCode.METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED
                ResultCode.CONFLICT -> HttpStatus.CONFLICT
                else -> HttpStatus.BAD_REQUEST
            }
            "5" -> HttpStatus.INTERNAL_SERVER_ERROR
            else -> HttpStatus.OK
        }
    }
}
