package com.hoji.common.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 날짜/시간 유틸리티
 */
object DateTimeUtils {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * LocalDateTime을 문자열로 변환
     */
    fun format(dateTime: LocalDateTime?, pattern: String = "yyyy-MM-dd HH:mm:ss"): String? {
        return dateTime?.format(DateTimeFormatter.ofPattern(pattern))
    }

    /**
     * LocalDate를 문자열로 변환
     */
    fun format(date: LocalDate?, pattern: String = "yyyy-MM-dd"): String? {
        return date?.format(DateTimeFormatter.ofPattern(pattern))
    }

    /**
     * 문자열을 LocalDateTime으로 변환
     */
    fun parseDateTime(dateTimeString: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern))
    }

    /**
     * 문자열을 LocalDate로 변환
     */
    fun parseDate(dateString: String, pattern: String = "yyyy-MM-dd"): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern))
    }

    /**
     * 두 날짜 사이의 일수 계산
     */
    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }

    /**
     * 두 시간 사이의 시간 차이 계산 (초)
     */
    fun secondsBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }

    /**
     * 현재 날짜/시간을 특정 포맷으로 반환
     */
    fun nowFormatted(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    }

    /**
     * 날짜가 특정 범위 내에 있는지 확인
     */
    fun isWithinRange(target: LocalDate, start: LocalDate, end: LocalDate): Boolean {
        return !target.isBefore(start) && !target.isAfter(end)
    }
}
