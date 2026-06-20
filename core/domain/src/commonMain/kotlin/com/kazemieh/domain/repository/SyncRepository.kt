package com.kazemieh.domain.repository

import com.kazemieh.common.model.*
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    suspend fun uploadModifiedEntities()
    suspend fun downloadUpdates()
    suspend fun syncAll()
}
