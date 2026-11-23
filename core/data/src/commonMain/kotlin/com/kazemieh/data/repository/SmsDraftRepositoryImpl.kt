package com.kazemieh.data.repository

import com.kazemieh.common.model.SmsDraft
import com.kazemieh.data_contract.datasource.SmsDraftLocalDataSource
import com.kazemieh.domain.repository.SmsDraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.kazemieh.common.model.SMS_IGNORED_RETENTION_MILLIS
import kotlin.time.Clock

class SmsDraftRepositoryImpl(
    private val localDataSource: SmsDraftLocalDataSource,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : SmsDraftRepository {
    override suspend fun addSmsDraft(smsDraft: SmsDraft): Long {
        return localDataSource.insertSmsDraft(smsDraft)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUnusedSmsDrafts(): Flow<List<SmsDraft>> =
        localDataSource.observeUnusedSmsDrafts().transformLatest { drafts ->
            // Time alone can expire a card while the sheet remains open. A DB write
            // cancels this timer immediately; checking at least every minute also
            // notices wall-clock changes without relying on a background Worker.
            while (true) {
                val now = nowMillis()
                val active = drafts.filterNot { it.isExpired(now) }
                emit(active)
                val firstIgnored = active.mapNotNull { it.ignoredAt }.minOrNull() ?: break
                val deadline = firstIgnored.coerceAtMost(Long.MAX_VALUE - SMS_IGNORED_RETENTION_MILLIS) + SMS_IGNORED_RETENTION_MILLIS
                delay((deadline - now).coerceIn(1L, 60_000L))
            }
        }.onStart { deleteExpiredIgnoredSmsDrafts() }.distinctUntilChanged()

    private fun SmsDraft.isExpired(now: Long): Boolean = !isUsed && ignoredAt?.let {
        it <= now.coerceAtLeast(Long.MIN_VALUE + SMS_IGNORED_RETENTION_MILLIS) - SMS_IGNORED_RETENTION_MILLIS
    } == true

    override suspend fun ignoreSmsDraft(id: Long) = localDataSource.ignoreSmsDraft(id, nowMillis())

    override suspend fun deleteExpiredIgnoredSmsDrafts() = localDataSource.deleteExpiredIgnoredSmsDrafts(
        nowMillis().coerceAtLeast(Long.MIN_VALUE + SMS_IGNORED_RETENTION_MILLIS) - SMS_IGNORED_RETENTION_MILLIS
    )

    override suspend fun markSmsDraftAsUsed(id: Long) {
        localDataSource.markSmsDraftAsUsed(id)
    }

    override suspend fun deleteSmsDraft(id: Long) {
        localDataSource.deleteSmsDraft(id)
    }

    override suspend fun deleteUnusedSmsDrafts() {
        localDataSource.deleteUnusedSmsDrafts()
    }

    override suspend fun deleteUnusedSmsDrafts(ids: List<Long>): List<Long> =
        localDataSource.deleteUnusedSmsDrafts(ids)

    override suspend fun getSmsDraftById(id: Long): SmsDraft? {
        return localDataSource.getSmsDraftById(id)?.takeUnless { it.isExpired(nowMillis()) }
    }

    override suspend fun updateSmsDraft(smsDraft: SmsDraft) {
        localDataSource.updateSmsDraft(smsDraft)
    }
}
