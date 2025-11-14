package com.hoji.controller

import com.hoji.common.dto.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Health Check 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    @GetMapping
    fun health(): ApiResponse<HealthResponse> {
        logger.info { "Health check requested" }
        val response = HealthResponse(
            status = "UP",
            timestamp = LocalDateTime.now(),
            application = "hoji"
        )
        return ApiResponse.success(response)
    }
}

data class HealthResponse(
    val status: String,
    val timestamp: LocalDateTime,
    val application: String
)
