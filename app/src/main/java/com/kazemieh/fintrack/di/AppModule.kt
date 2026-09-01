package com.kazemieh.fintrack.di

import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.CrashReporter
import com.kazemieh.fintrack.analytics.AndroidAnalyticsService
import com.kazemieh.fintrack.analytics.AndroidCrashReporter
import org.koin.dsl.module

val appModule = module {
    single<AnalyticsService> { AndroidAnalyticsService(get()) }
    single<CrashReporter> { AndroidCrashReporter() }
}
