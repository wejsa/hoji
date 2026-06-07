package com.jwtstarter.common.exception

import com.jwtstarter.common.dto.ResultCode

/**
 * 비즈니스 로직 예외
 */
open class BusinessException(
    val resultCode: ResultCode,
    override val message: String = resultCode.message,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    constructor(resultCode: ResultCode, cause: Throwable?) : this(resultCode, resultCode.message, cause)
}

/**
 * 리소스를 찾을 수 없는 경우
 */
class NotFoundException(
    message: String = ResultCode.NOT_FOUND.message
) : BusinessException(ResultCode.NOT_FOUND, message)

/**
 * 유효하지 않은 입력값
 */
class InvalidInputException(
    message: String = ResultCode.INVALID_INPUT.message
) : BusinessException(ResultCode.INVALID_INPUT, message)

/**
 * 권한이 없는 경우
 */
class UnauthorizedException(
    message: String = ResultCode.UNAUTHORIZED.message
) : BusinessException(ResultCode.UNAUTHORIZED, message)

/**
 * 접근이 금지된 경우
 */
class ForbiddenException(
    message: String = ResultCode.FORBIDDEN.message
) : BusinessException(ResultCode.FORBIDDEN, message)

/**
 * 충돌이 발생한 경우
 */
class ConflictException(
    message: String = ResultCode.CONFLICT.message
) : BusinessException(ResultCode.CONFLICT, message)
