package com.kazemieh.domain.usecase

import com.kazemieh.common.analytics.CrashReporter
import com.kazemieh.common.analytics.NoOpCrashReporter
import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

enum class SmsPreparationIssue { CATEGORY, SOURCE, CURRENCY, AMOUNT, TYPE, UNAVAILABLE }

sealed interface SmsDraftPreparation {
    data class Ready(val transaction: Transaction) : SmsDraftPreparation
    data class NeedsInput(val issue: SmsPreparationIssue) : SmsDraftPreparation
}

/** Shared readiness contract for the displayed count, one-card registration and batch registration. */
fun prepareSmsDraftTransaction(draft: SmsDraft, categories: List<Category>, sources: List<Source>,
    currencyCode: String): SmsDraftPreparation {
    fun missing(issue: SmsPreparationIssue) = SmsDraftPreparation.NeedsInput(issue)
    if (draft.isUsed) return missing(SmsPreparationIssue.UNAVAILABLE)
    if (draft.type !in setOf(TransactionType.EXPENSE, TransactionType.INCOME)) return missing(SmsPreparationIssue.TYPE)
    if (reviewSmsAmount(draft) != null || draft.amount <= 0) return missing(SmsPreparationIssue.AMOUNT)
    val categoryId = draft.categoryId ?: return missing(SmsPreparationIssue.CATEGORY)
    val category = categories.singleOrNull { it.id == categoryId && it.syncStatus != SyncStatus.DELETED && it.type == draft.type }
        ?: return missing(SmsPreparationIssue.CATEGORY)
    val source = resolveSmsSource(draft, sources.filter { it.syncStatus != SyncStatus.DELETED })
        ?: return missing(SmsPreparationIssue.SOURCE)
    if (!isSmsSourceCurrencyCompatible(source, currencyCode)) return missing(SmsPreparationIssue.CURRENCY)
    val amount = if (currencyCode == "IRT") draft.amount / 10 else draft.amount
    if (amount <= 0) return missing(SmsPreparationIssue.AMOUNT)
    return SmsDraftPreparation.Ready(Transaction(id = 0, amount = amount, currencyCode = currencyCode,
        categoryId = requireNotNull(category.id), sourceId = requireNotNull(source.id), description = draft.body,
        timeStamp = draft.timeStamp, date = draft.date, type = draft.type))
}

enum class SmsRegistrationStatus { POSTED, INCOMPLETE, FAILED, UNAVAILABLE }
data class SmsRegistrationResult(val draftId: Long, val status: SmsRegistrationStatus,
    val issue: SmsPreparationIssue? = null, val transactionId: Long? = null)
data class SmsBatchProgress(val total: Int, val results: List<SmsRegistrationResult> = emptyList()) {
    val processed: Int get() = results.size
    val posted: Int get() = results.count { it.status == SmsRegistrationStatus.POSTED }
    val incomplete: Int get() = results.count { it.status == SmsRegistrationStatus.INCOMPLETE }
    val failed: Int get() = results.count { it.status == SmsRegistrationStatus.FAILED }
    val unavailable: Int get() = results.count { it.status == SmsRegistrationStatus.UNAVAILABLE }
}

class RegisterSmsDraftsUseCase(
    private val drafts: SmsDraftRepository,
    private val transactions: TransactionRepository,
    private val addTransaction: AddTransactionUseCase,
    private val crashReporter: CrashReporter = NoOpCrashReporter()
) {
    suspend fun registerOne(id: Long, currencyCode: String): SmsRegistrationResult {
        try {
            val draft = drafts.getSmsDraftById(id)
            if (draft == null || draft.isUsed) return SmsRegistrationResult(id, SmsRegistrationStatus.UNAVAILABLE)
            val category = draft.categoryId?.let { transactions.getCategoryById(it) }
            val prepared = prepareSmsDraftTransaction(draft, listOfNotNull(category), transactions.observeSources().first(), currencyCode)
            if (prepared is SmsDraftPreparation.NeedsInput)
                return SmsRegistrationResult(id, SmsRegistrationStatus.INCOMPLETE, prepared.issue)
            val transactionId = addTransaction((prepared as SmsDraftPreparation.Ready).transaction,
                emptyList(), emptyList(), smsDraftId = id)
            if (transactionId > 0) return SmsRegistrationResult(id, SmsRegistrationStatus.POSTED, transactionId = transactionId)
            val current = drafts.getSmsDraftById(id)
            return SmsRegistrationResult(id, if (current == null || current.isUsed)
                SmsRegistrationStatus.UNAVAILABLE else SmsRegistrationStatus.FAILED)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { crashReporter.recordException(e, "sms_registration_failed") }
            return SmsRegistrationResult(id, SmsRegistrationStatus.FAILED)
        }
    }

    suspend operator fun invoke(ids: List<Long>, currencyCode: String,
        onProgress: suspend (SmsBatchProgress) -> Unit = {}): SmsBatchProgress {
        val selected = ids.distinct()
        val results = mutableListOf<SmsRegistrationResult>()
        onProgress(SmsBatchProgress(selected.size))
        for (id in selected) {
            results += registerOne(id, currencyCode)
            onProgress(SmsBatchProgress(selected.size, results.toList()))
        }
        return SmsBatchProgress(selected.size, results.toList())
    }
}
