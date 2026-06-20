package com.kazemieh.sync.di

import com.kazemieh.sync.GoogleDriveSyncManager
import com.kazemieh.sync.ServerSyncManager
import com.kazemieh.sync.ui.SyncViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val syncModule = module {
    single { GoogleDriveSyncManager(get()) }
    single { ServerSyncManager(get(), get()) }
    viewModel { SyncViewModel(get(), get(), get()) }
}
