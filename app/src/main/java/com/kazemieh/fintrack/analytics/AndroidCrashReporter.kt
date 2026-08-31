package com.kazemieh.fintrack.analytics

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.kazemieh.common.analytics.CrashReporter

class AndroidCrashReporter : CrashReporter {
    private val crashlytics = Firebase.crashlytics

    override fun recordException(error: Throwable, safeMessage: String?) {
        safeMessage?.let {
            crashlytics.log("Safe Context: $it")
        }
        crashlytics.recordException(error)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
