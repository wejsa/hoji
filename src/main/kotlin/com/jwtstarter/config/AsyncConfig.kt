package com.jwtstarter.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

private val logger = KotlinLogging.logger {}

/**
 * 비동기 처리 설정
 */
@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        // 코어 스레드 수
        executor.corePoolSize = 5

        // 최대 스레드 수
        executor.maxPoolSize = 10

        // 큐 용량
        executor.queueCapacity = 25

        // 스레드 이름 prefix
        executor.setThreadNamePrefix("async-")

        // 종료 대기 설정 (graceful shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)

        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { throwable, method, params ->
            logger.error(throwable) {
                "비동기 메서드 실행 중 예외 발생 - Method: ${method.name}, Params: ${params.contentToString()}"
            }
        }
    }
}
