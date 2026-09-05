package com.kazemieh.network.di

import com.kazemieh.network.createHttpClient
import com.kazemieh.network.service.AiChatService
import com.kazemieh.network.service.RssNewsService
import com.kazemieh.network.service.SyncService
import com.kazemieh.network.service.TgjuService
import com.kazemieh.network.service.NobitexService
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single { TgjuService(get()) }
    single { NobitexService(get()) }
    single { SyncService(get()) }
    single { AiChatService() }
    single { RssNewsService(get()) }
}
