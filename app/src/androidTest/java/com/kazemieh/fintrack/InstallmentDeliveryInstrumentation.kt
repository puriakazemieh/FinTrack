package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.Notification
import android.app.NotificationManager as SystemNotifications
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.work.*
import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.canonicalNotificationChannel
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.notifications.NotificationWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.java.KoinJavaComponent.getKoin

/** Exercises real queued jobs and fresh records; never invokes a financial action. Emulator only. */
class InstallmentDeliveryInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val failures = mutableListOf<String>()
        val owned = mutableListOf<Long>()
        val names = mutableListOf<String>()
        val cardIds = mutableListOf<Int>()
        var tests = 0
        var repository: InstallmentRepository? = null
        var publisher: NotificationManager? = null
        var work: WorkManager? = null
        try {
            check(Build.VERSION.SDK_INT == 31 && (Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")))
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<InstallmentRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository)
            val notifications = getKoin().get<NotificationManager>().also { publisher = it }
            val scheduler = getKoin().get<NotificationScheduler>()
            val manager = WorkManager.getInstance(targetContext).also { work = it }
            val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotifications
            check(system.areNotificationsEnabled())
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val source = runBlocking { getKoin().get<GetDefaultFinancialSourceUseCase>()()?.id }!!
            val marker = "QA-INSTALLMENT-${System.currentTimeMillis()}"
            fun scenario(name: String, legacy: Boolean = false, invalid: Boolean = false,
                mutate: (Installment) -> Unit = {}, expectedCard: Boolean = false) {
                tests++
                try {
                    val due = System.currentTimeMillis() + 3000
                    val id = runBlocking { records.insertInstallment(Installment(id = 0, title = "$marker-$name",
                        totalAmount = 3000, installmentAmount = 1000, totalInstallments = 3, paidInstallments = 0,
                        categoryId = category, sourceId = source, startDate = due, nextDueDate = due,
                        frequency = InstallmentFrequency.MONTHLY, reminderEnabled = true, postAsTransaction = false),
                        emptyList(), emptyList()) }.also { owned += it }
                    val model = runBlocking { records.getInstallmentById(id)!! }
                    val key = "installment_$id".also { names += it }
                    val notificationId = key.hashCode().also { cardIds += it }
                    check(system.activeNotifications.none { it.id == notificationId }) { "Notification id collision" }
                    if (legacy || invalid) {
                        manager.enqueueUniqueWork(key, ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<NotificationWorker>().setInitialDelay(3, TimeUnit.SECONDS)
                                .setInputData(NotificationWorker.createInputData("STALE TITLE", "STALE BODY",
                                    "installment_reminders", notificationId, if (invalid) 0L else null)).build()).result.get()
                    } else {
                        scheduler.scheduleReminder(key, "STALE TITLE", "STALE BODY",
                            Instant.fromEpochMilliseconds(due).toLocalDateTime(TimeZone.currentSystemDefault()),
                            NotificationManager.CHANNEL_INSTALLMENT, id)
                    }
                    mutate(model)
                    val deadline = SystemClock.elapsedRealtime() + 60000
                    var completed = false
                    while (SystemClock.elapsedRealtime() < deadline) {
                        val jobs = manager.getWorkInfosForUniqueWork(key).get(10, TimeUnit.SECONDS)
                        if (jobs.isNotEmpty() && jobs.all { it.state == WorkInfo.State.SUCCEEDED }) { completed = true; break }
                        check(jobs.none { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED })
                        Thread.sleep(150)
                    }
                    check(completed) { "Real Worker did not succeed" }
                    val cards = system.activeNotifications.filter { it.id == notificationId &&
                        canonicalNotificationChannel(it.notification.channelId) == NotificationManager.CHANNEL_INSTALLMENT }
                    if (expectedCard) {
                        val card = cards.single().notification
                        check(card.actions?.size == 2) { "Installment actions absent" }
                        val text = card.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                        val current = runBlocking { records.getInstallmentById(id)!! }
                        check(text.contains(current.title) && !text.contains("STALE")) { "Stale reminder: $text" }
                        if (!legacy) check(text.contains("۷,۰۰۰") && text.contains('$')) { "Wrong amount/currency: $text" }
                    } else check(cards.isEmpty()) { "$name still produced an actionable/visible notification" }
                } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}" }
            }
            scenario("fresh-record", mutate = { model -> runBlocking {
                records.updateInstallment(model.copy(title = "$marker-edited", installmentAmount = 7000, currencyCode = "USD"),
                    emptyList(), emptyList())
            } }, expectedCard = true)
            scenario("legacy-without-id", legacy = true, expectedCard = true)
            for (kind in listOf("disabled", "completed", "deleted", "postponed", "different-past-period")) {
                scenario(kind, mutate = { model -> runBlocking {
                    when (kind) {
                        "deleted" -> records.deleteInstallment(model.id)
                        else -> records.updateInstallment(when (kind) {
                            "disabled" -> model.copy(reminderEnabled = false)
                            "completed" -> model.copy(isCompleted = true, paidInstallments = model.totalInstallments)
                            "postponed" -> model.copy(nextDueDate = model.nextDueDate + 3600000)
                            else -> model.copy(nextDueDate = model.nextDueDate - 60000)
                        }, emptyList(), emptyList())
                    }
                } })
            }
            scenario("invalid-explicit-id", invalid = true)
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finally {
            names.forEach { name -> runCatching { work?.cancelUniqueWork(name)?.result?.get() } }
            cardIds.zip(owned).forEach { (card, record) -> runCatching {
                publisher?.cancelNotification(card, NotificationManager.CHANNEL_INSTALLMENT, record)
            } }
            owned.forEach { id -> runCatching { runBlocking { repository?.deleteInstallment(id) } } }
        }
        finish(if (failures.isEmpty() && tests == 8) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n")); putString("record_ids", owned.joinToString(","))
        })
    }
}
