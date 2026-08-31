package com.kazemieh.common.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCommonModule: Module = module {
    single<com.kazemieh.common.analytics.AnalyticsService> { com.kazemieh.common.analytics.NoOpAnalyticsService() }
    single<com.kazemieh.common.analytics.CrashReporter> { com.kazemieh.common.analytics.NoOpCrashReporter() }
}
