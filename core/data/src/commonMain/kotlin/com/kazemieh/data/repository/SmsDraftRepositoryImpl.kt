package com.kazemieh.data.repository

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.data_contract.datasource.SmsDraftLocalDataSource
import com.kazemieh.domain.repository.SmsDraftRepository
import kotlinx.coroutines.flow.Flow

class SmsDraftRepositoryImpl(
    private val localDataSource: SmsDraftLocalDataSource
) : SmsDraftRepository {
    override suspend fun addSmsDraft(smsDraft: SmsDraft): Long {
        return localDataSource.insertSmsDraft(smsDraft)
    }

    override fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>> {
        return localDataSource.observeUnusedSmsDrafts()
    }

    override suspend fun markSmsDraftAsUsed(id: Long) {
        localDataSource.markSmsDraftAsUsed(id)
    }

    override suspend fun deleteSmsDraft(id: Long) {
        localDataSource.deleteSmsDraft(id)
    }

    override suspend fun getSmsDraftById(id: Long): SmsDraft? {
        return localDataSource.getSmsDraftById(id)
    }

    override suspend fun updateSmsDraft(smsDraft: SmsDraft) {
        localDataSource.updateSmsDraft(smsDraft)
    }
}
