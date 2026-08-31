package com.kazemieh.common.analytics

enum class AnalyticsConsent {
    GRANTED,
    DENIED,
    UNKNOWN
}

interface AnalyticsService {
    fun track(event: ProductEvent)
    fun setConsent(consent: AnalyticsConsent)
    fun setUserId(userId: String)
}

interface CrashReporter {
    fun recordException(error: Throwable, safeMessage: String? = null)
    fun log(message: String)
    fun setCustomKey(key: String, value: String)
}

interface Trace {
    fun stop()
}

interface PerformanceTracer {
    fun startTrace(traceName: String): Trace
}
