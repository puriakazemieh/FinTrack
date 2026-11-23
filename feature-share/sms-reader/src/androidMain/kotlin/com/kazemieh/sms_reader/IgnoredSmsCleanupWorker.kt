package com.kazemieh.sms_reader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kazemieh.domain.repository.SmsDraftRepository
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class IgnoredSmsCleanupWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {
    private val drafts: SmsDraftRepository by inject()

    override suspend fun doWork(): Result = try {
        drafts.deleteExpiredIgnoredSmsDrafts()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_NAME = "IgnoredSmsCleanup"
        fun enqueuePeriodicWork(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<IgnoredSmsCleanupWorker>(12, TimeUnit.HOURS).build())
        }
    }
}
