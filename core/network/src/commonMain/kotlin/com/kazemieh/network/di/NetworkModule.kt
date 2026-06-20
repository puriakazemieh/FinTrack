package com.kazemieh.network.di

import com.kazemieh.network.createHttpClient
import com.kazemieh.network.service.SyncService
import com.kazemieh.network.service.TgjuService
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single { TgjuService(get()) }
    single { SyncService(get()) }
}
