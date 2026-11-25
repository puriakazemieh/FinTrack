package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.Notification
import android.app.NotificationManager as SystemNotifications
import android.os.Build
import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.notification.canonicalNotificationChannel
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.notifications.NotificationWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.java.KoinJavaComponent.getKoin

/** Real WorkManager delivery probes. Never posts a financial transaction. Emulator only. */
class FixedExpenseDeliveryInstrumentation : Instrumentation() {
    private var warmStart = false
    override fun onCreate(arguments: Bundle?) {
        warmStart = arguments?.getString("warm") == "true"
        super.onCreate(arguments); start()
    }
    override fun onStart() {
        val ids = mutableListOf<Long>()
        val jobs = mutableListOf<String>()
        val failures = mutableListOf<String>()
        var tests = 0
        var repository: FixedExpenseRepository? = null
        var publisher: NotificationManager? = null
        var work: WorkManager? = null
        var deliveredTitle = ""
        var deliveredBody = ""
        var invalidNotificationId: Int? = null
        val marker = "QA-FIXED-DELIVERY-${System.currentTimeMillis()}"
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<FixedExpenseRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository)
            val notifications = getKoin().get<NotificationManager>().also { publisher = it }
            notifications.createChannels()
            check(notifications.hasPermission()) { "Notification permission required" }
            val scheduler = getKoin().get<NotificationScheduler>()
            val manager = WorkManager.getInstance(targetContext).also { work = it }
            val system = targetContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as SystemNotifications
            if (warmStart) {
                startActivitySync(android.content.Intent(targetContext, MainActivity::class.java)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                Thread.sleep(2500)
                check(java.util.Locale.getDefault().language == "fa") { "Warm process language differs from Persian preference" }
            }
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val source = runBlocking { getKoin().get<GetDefaultFinancialSourceUseCase>()()?.id }!!
            fun test(name: String, block: () -> Unit) {
                tests++
                try { block() } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}" }
            }
            fun create(name: String, due: Long): FixedExpense {
                val model = FixedExpense(title = "$marker-$name", amount = 1000L, categoryId = category,
                    sourceId = source, recurrence = RecurrenceType.MONTHLY, startDate = due,
                    nextDueDate = due + 86_400_000L, reminderEnabled = true, reminderTime = due,
                    isAutoPostEnabled = false)
                val id = runBlocking { records.insertFixedExpense(model) }.also { ids += it }
                return runBlocking { records.getFixedExpenseById(id)!! }
            }
            fun infos(name: String) = manager.getWorkInfosForUniqueWork(name).get()
            fun done(name: String, previous: Set<java.util.UUID>) {
                repeat(200) {
                    val current = infos(name).filter { it.id !in previous }
                    if (current.isNotEmpty() && current.all { it.state == WorkInfo.State.SUCCEEDED }) return
                    if (current.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED })
                        error("Worker failed/cancelled: ${current.map { it.state }}")
                    Thread.sleep(150)
                }
                error("Worker did not finish: ${infos(name).map { it.state }}")
            }
            fun schedule(model: FixedExpense): Pair<String, Set<java.util.UUID>> {
                val name = "fixed_expense_${model.id}".also { jobs += it }
                val prior = infos(name).map { it.id }.toSet()
                scheduler.scheduleReminder(name, "STALE TITLE", "STALE BODY",
                    Instant.fromEpochMilliseconds(model.reminderTime!!).toLocalDateTime(TimeZone.currentSystemDefault()),
                    NotificationManager.CHANNEL_FIXED_EXPENSE, model.id)
                return name to prior
            }
            fun cards(id: Long) = system.activeNotifications.filter {
                it.id == "fixed_expense_$id".hashCode() &&
                    canonicalNotificationChannel(it.notification.channelId) == NotificationManager.CHANNEL_FIXED_EXPENSE
            }
            test("current title amount and currency replace the old payload") {
                val model = create("original", System.currentTimeMillis() + 4_000L)
                val request = schedule(model)
                runBlocking { records.updateFixedExpense(model.copy(title = "$marker-edited", amount = 7000L, currencyCode = "USD")) }
                done(request.first, request.second)
                val card = cards(model.id).single().notification
                deliveredTitle = card.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
                deliveredBody = card.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                check(deliveredTitle == "هزینه‌های ثابت") { "Stale/wrong title: $deliveredTitle" }
                check(deliveredBody.contains("$marker-edited") && deliveredBody.contains("۷,۰۰۰") && deliveredBody.contains('$')) {
                    "Stale/wrong record amount or currency: $deliveredBody"
                }
            }
            for (kind in listOf("disabled", "inactive", "deleted", "postponed", "different-past-occurrence")) {
                test("$kind record suppresses its queued old job") {
                    val model = create(kind, System.currentTimeMillis() + 4_000L)
                    val request = schedule(model)
                    runBlocking {
                        when (kind) {
                            "disabled" -> records.updateFixedExpense(model.copy(reminderEnabled = false))
                            "inactive" -> records.updateFixedExpense(model.copy(isActive = false))
                            "deleted" -> records.deleteFixedExpense(model.id)
                            "postponed" -> records.updateFixedExpense(model.copy(reminderTime = model.reminderTime!! + 3_600_000L))
                            else -> records.updateFixedExpense(model.copy(reminderTime = model.reminderTime!! - 60_000L))
                        }
                    }
                    done(request.first, request.second)
                    check(cards(model.id).isEmpty()) { "Suppressed record still produced an Android notification" }
                }
            }
            test("legacy job without a record id resolves a unique current record") {
                val model = create("legacy", System.currentTimeMillis() - 1_000L)
                val name = "fixed_expense_${model.id}".also { jobs += it }
                val previous = infos(name).map { it.id }.toSet()
                manager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInputData(NotificationWorker.createInputData("OLD LEGACY TITLE", "OLD LEGACY BODY",
                        NotificationManager.CHANNEL_FIXED_EXPENSE, name.hashCode())).build()).result.get()
                done(name, previous)
                val text = cards(model.id).single().notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                check(text.contains(model.title)) { "Legacy worker did not use current record: $text" }
            }
            test("invalid explicit record id never falls back to another record") {
                val name = "$marker-invalid".also { jobs += it }
                val notificationId = name.hashCode().also { invalidNotificationId = it }
                check(system.activeNotifications.none { it.id == notificationId }) { "Probe id collision" }
                val previous = infos(name).map { it.id }.toSet()
                manager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInputData(NotificationWorker.createInputData("INVALID", "INVALID",
                        NotificationManager.CHANNEL_FIXED_EXPENSE, notificationId, 0L, System.currentTimeMillis() - 1_000)).build()).result.get()
                done(name, previous)
                check(system.activeNotifications.none { it.id == notificationId }) { "Invalid record produced a notification" }
            }
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finally {
            jobs.forEach { name -> runCatching { work?.cancelUniqueWork(name)?.result?.get() } }
            ids.forEach { id ->
                runCatching { publisher?.cancelNotification("fixed_expense_$id".hashCode(), NotificationManager.CHANNEL_FIXED_EXPENSE, id) }
                runCatching { runBlocking { repository?.deleteFixedExpense(id) } }
            }
            invalidNotificationId?.let { id -> runCatching { publisher?.cancelNotification(id, NotificationManager.CHANNEL_FIXED_EXPENSE, null) } }
        }
        finish(if (failures.isEmpty() && tests == 8) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n")); putString("record_ids", ids.joinToString(","))
            putString("delivered_title", deliveredTitle); putString("delivered_body", deliveredBody)
        })
    }
}
