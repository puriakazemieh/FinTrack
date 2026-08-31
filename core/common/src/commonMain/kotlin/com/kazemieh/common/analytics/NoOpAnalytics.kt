package com.kazemieh.common.analytics

class NoOpAnalyticsService : AnalyticsService {
    override fun track(event: ProductEvent) {}
    override fun setConsent(consent: AnalyticsConsent) {}
    override fun setUserId(userId: String) {}
}

class NoOpCrashReporter : CrashReporter {
    override fun recordException(error: Throwable, safeMessage: String?) {}
    override fun log(message: String) {}
    override fun setCustomKey(key: String, value: String) {}
}

class NoOpPerformanceTracer : PerformanceTracer {
    override fun startTrace(traceName: String): Trace = object : Trace {
        override fun stop() {}
    }
}
