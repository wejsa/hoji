package com.hoji.common.context

/**
 * 요청 컨텍스트 정보
 */
data class RequestContext(
    val requestId: String,
    val userId: String? = null,
    val userAgent: String? = null,
    val remoteAddr: String? = null,
    val headers: Map<String, String> = emptyMap()
) {
    companion object {
        private val contextHolder = ThreadLocal<RequestContext>()

        fun set(context: RequestContext) {
            contextHolder.set(context)
        }

        fun get(): RequestContext? {
            return contextHolder.get()
        }

        fun clear() {
            contextHolder.remove()
        }

        fun getRequestId(): String? {
            return get()?.requestId
        }

        fun getUserId(): String? {
            return get()?.userId
        }

        fun getHeader(key: String): String? {
            return get()?.headers?.get(key)
        }
    }
}
