package com.kazemieh.sync.di

import com.kazemieh.sync.GoogleDriveSyncManager
import com.kazemieh.sync.ServerSyncManager
import com.kazemieh.sync.ui.SyncViewModel
import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.network.service.SyncService
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val syncModule = module {
    single { GoogleDriveSyncManager(get<BackupRepository>()) }
    single { ServerSyncManager(get<BackupRepository>(), get<SyncService>()) }
    viewModelOf(::SyncViewModel)
}
