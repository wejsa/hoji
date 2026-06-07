package com.hoji.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * JSON 유틸리티
 */
object JsonUtils {

    // inline fun(fromJson/fromMap)에서 접근하므로 @PublishedApi internal 노출
    @PublishedApi
    internal val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    fun toJson(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    /**
     * 객체를 Pretty JSON 문자열로 변환
     */
    fun toPrettyJson(obj: Any): String {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }

    /**
     * JSON 문자열을 객체로 변환
     */
    inline fun <reified T> fromJson(json: String): T {
        return objectMapper.readValue(json)
    }

    /**
     * Map을 객체로 변환
     */
    inline fun <reified T> fromMap(map: Map<String, Any>): T {
        return objectMapper.convertValue(map, T::class.java)
    }

    /**
     * 객체를 Map으로 변환
     */
    fun toMap(obj: Any): Map<String, Any> {
        return objectMapper.convertValue(obj, Map::class.java) as Map<String, Any>
    }

    /**
     * JSON 문자열 유효성 검사
     */
    fun isValidJson(json: String): Boolean {
        return try {
            objectMapper.readTree(json)
            true
        } catch (e: Exception) {
            false
        }
    }
}
