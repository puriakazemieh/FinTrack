package com.kazemieh.data.repository

import com.kazemieh.domain.repository.SyncRepository
import com.kazemieh.data_contract.datasource.SyncLocalDataSource

class SyncRepositoryImpl(
    private val syncLocalDataSource: SyncLocalDataSource
) : SyncRepository {
    override suspend fun uploadModifiedEntities() {
        val modified = syncLocalDataSource.getModifiedEntities()
        // TODO: Upload to cloud
    }

    override suspend fun downloadUpdates() {
        // TODO: Download from cloud
    }

    override suspend fun syncAll() {
        uploadModifiedEntities()
        downloadUpdates()
    }
}
