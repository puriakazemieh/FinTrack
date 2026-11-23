package com.kazemieh.fintrack

import android.app.Instrumentation
import android.app.NotificationManager as SystemNotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.service.notification.StatusBarNotification
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.Transaction
import com.kazemieh.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.kazemieh.notifications.AndroidNotificationManager
import org.koin.java.KoinJavaComponent.getKoin

/** Runs against the installed Android publisher without a separate test-library dependency. */
class NotificationIdentityInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val failures = mutableListOf<String>()
        var tests = 0
        fun test(name: String, action: () -> Unit) {
            tests++
            try { action() } catch (failure: Throwable) {
                failures += "$name: ${failure.message}"
            }
        }
        val system = targetContext.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotificationManager
        // Instrumentation.onCreate precedes Application.onCreate; wait for the target's own DI.
        var preferences: PreferenceUseCases? = null
        for (attempt in 0 until 100) {
            preferences = runCatching { getKoin().get<PreferenceUseCases>() }.getOrNull()
            if (preferences != null) break
            Thread.sleep(100)
        }
        if (preferences == null) {
            finish(0, Bundle().apply { putString("setup_failure", "Target application DI did not become ready") })
            return
        }
        val publisher = AndroidNotificationManager(targetContext, preferences, NoOpAnalyticsService())
        publisher.createChannels()
        check(publisher.hasPermission()) { "Grant POST_NOTIFICATIONS to the emulator target before running" }

        test("nine notification types keep independent cards with the same integer ID") {
            withUnusedId(system, NotificationManager.ID_QUICK_ADD) {
                val delivery = targetContext.getSharedPreferences(AndroidNotificationManager.DELIVERY_PREFERENCES, Context.MODE_PRIVATE)
                val hadDismissal = delivery.contains(AndroidNotificationManager.QUICK_ADD_DISMISSED)
                val dismissal = delivery.getBoolean(AndroidNotificationManager.QUICK_ADD_DISMISSED, false)
                delivery.edit().remove(AndroidNotificationManager.QUICK_ADD_DISMISSED).commit()
                try {
                    val channels = listOf(NotificationManager.CHANNEL_BUDGET, NotificationManager.CHANNEL_CHEQUE,
                        NotificationManager.CHANNEL_DEBT, NotificationManager.CHANNEL_SHOPPING,
                        NotificationManager.CHANNEL_NOTE, NotificationManager.CHANNEL_FIXED_EXPENSE)
                    channels.forEachIndexed { index, channel ->
                        publisher.showNotification(9001, "QA identity $channel", "Synthetic identity probe", channel, 100L + index)
                    }
                    publisher.showStickyNotification(9001, "QA SMS identity", "Synthetic identity probe", 107)
                    publisher.showInstallmentNotification(9001, "QA installment identity", "Synthetic identity probe", 108)
                    publisher.showQuickAddNotification("QA quick-add identity", "Synthetic identity probe", "QA open")
                    Thread.sleep(500)
                    val cards = system.activeNotifications.filter { it.id == 9001 }
                    check(cards.size == 9) { "Expected nine cards, actual ${cards.size}" }
                    check(cards.map { it.notification.deleteIntent }.toSet().size == 9) { "Dismiss callbacks overlap" }
                    publisher.cancelNotification(9001)
                    awaitCondition { system.activeNotifications.count { it.id == 9001 } == 8 }
                    channels.forEachIndexed { index, channel -> publisher.cancelNotification(9001, channel, 100L + index) }
                    publisher.cancelNotification(9001, NotificationManager.CHANNEL_SMS, 107)
                    publisher.cancelNotification(9001, NotificationManager.CHANNEL_INSTALLMENT, 108)
                    awaitCondition { system.activeNotifications.none { it.id == 9001 } }
                } finally {
                    val edit = delivery.edit()
                    if (hadDismissal) edit.putBoolean(AndroidNotificationManager.QUICK_ADD_DISMISSED, dismissal)
                    else edit.remove(AndroidNotificationManager.QUICK_ADD_DISMISSED)
                    edit.commit()
                }
            }
        }
        test("full SMS record IDs survive an integer-ID collision") {
            withUnusedId(system, 777001) {
                publisher.showStickyNotification(777001, "QA SMS first", "Synthetic identity probe", 42)
                publisher.showStickyNotification(777001, "QA SMS second", "Synthetic identity probe", 4_294_967_338)
                Thread.sleep(300)
                check(system.activeNotifications.count { it.id == 777001 } == 2) { "One SMS replaced another" }
            }
        }
        test("two SMS records have independent dismiss callbacks") {
            withUnusedId(system, 777002) {
                publisher.showStickyNotification(777002, "QA SMS first", "Synthetic identity probe", 42)
                val first = awaitCard(system, 777002)
                publisher.showStickyNotification(777002, "QA SMS second", "Synthetic identity probe", 4_294_967_338)
                Thread.sleep(300)
                val second = system.activeNotifications.filter { it.id == 777002 }.maxBy { it.postTime }
                check(first.notification.deleteIntent != second.notification.deleteIntent) { "Dismiss callback was reused" }
            }
        }
        test("opening one SMS does not dismiss the other SMS with the same integer ID") {
            startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            withUnusedId(system, 777004) {
                publisher.showStickyNotification(777004, "QA body first", "Synthetic identity probe", 42)
                val first = awaitCard(system, 777004)
                publisher.showStickyNotification(777004, "QA body second", "Synthetic identity probe", 4_294_967_338)
                awaitCondition { system.activeNotifications.count { it.id == 777004 } == 2 }
                first.notification.contentIntent.send()
                awaitCondition { system.activeNotifications.count { it.id == 777004 } == 1 }
                check(system.activeNotifications.single { it.id == 777004 }.notification.extras
                    .getCharSequence(android.app.Notification.EXTRA_TITLE).toString() == "QA body second")
            }
        }
        test("ignoring a real SMS draft keeps a same-number budget notification and does not post money") {
            val drafts = getKoin().get<SmsDraftRepository>()
            val transactions = getKoin().get<TransactionRepository>()
            val count = runBlocking { transactions.getTransactionCount() }
            withUnusedId(system, 777003) {
                val draftId = runBlocking { drafts.addSmsDraft(SmsDraft(sender = "QA identity", body = "بلوبانک برداشت 120,000 ریال کارت 2222",
                    amount = 120000, bankName = "Blu", type = TransactionType.EXPENSE,
                    timeStamp = System.currentTimeMillis(), date = "1405/08/26")) }
                try {
                    publisher.showStickyNotification(777003, "QA ignore SMS", "Synthetic identity probe", draftId)
                    val sms = awaitCard(system, 777003)
                    publisher.showNotification(777003, "QA budget stays", "Synthetic identity probe", NotificationManager.CHANNEL_BUDGET, 1234)
                    sms.notification.actions.last().actionIntent.send()
                    awaitCondition { runBlocking { drafts.getSmsDraftById(draftId)?.let { !it.isUsed && it.ignoredAt != null } == true } }
                    awaitCondition { system.activeNotifications.count { it.id == 777003 } == 1 }
                    check(system.activeNotifications.single { it.id == 777003 }.notification.extras
                        .getCharSequence(android.app.Notification.EXTRA_TITLE).toString() == "QA budget stays")
                    check(runBlocking { transactions.getTransactionCount() } == count) { "Ignoring SMS posted a financial transaction" }
                } finally { runBlocking { drafts.deleteSmsDraft(draftId) } }
            }
        }
        test("cheque debt and installment ignore actions remove only their own cards") {
            withUnusedId(system, 777008) {
                val actions = mutableListOf<android.app.PendingIntent>()
                val channels = listOf(NotificationManager.CHANNEL_CHEQUE, NotificationManager.CHANNEL_DEBT,
                    NotificationManager.CHANNEL_INSTALLMENT)
                channels.forEachIndexed { index, channel ->
                    val title = "QA independent ignore $index"
                    if (channel == NotificationManager.CHANNEL_INSTALLMENT)
                        publisher.showInstallmentNotification(777008, title, "Synthetic identity probe", 300L + index)
                    else publisher.showNotification(777008, title, "Synthetic identity probe", channel, 300L + index)
                    awaitCondition { system.activeNotifications.count { it.id == 777008 } == index + 1 }
                    val card = system.activeNotifications.single { it.id == 777008 && it.notification.extras
                        .getCharSequence(android.app.Notification.EXTRA_TITLE).toString() == title }
                    actions += card.notification.actions.last().actionIntent
                }
                check(actions.toSet().size == 3)
                actions.forEachIndexed { index, action ->
                    action.send()
                    awaitCondition { system.activeNotifications.count { it.id == 777008 } == 2 - index }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            test("typed cancellation removes a legacy SMS and preserves a new budget") {
                withUnusedId(system, 777005) {
                    system.notify(777005, android.app.Notification.Builder(targetContext, NotificationManager.CHANNEL_SMS)
                        .setSmallIcon(targetContext.applicationInfo.icon).setContentTitle("QA legacy SMS").build())
                    publisher.showNotification(777005, "QA budget stays", "Synthetic identity probe", NotificationManager.CHANNEL_BUDGET, 1235)
                    awaitCondition { system.activeNotifications.count { it.id == 777005 } == 2 }
                    publisher.cancelNotification(777005, NotificationManager.CHANNEL_SMS, 42)
                    awaitCondition { system.activeNotifications.count { it.id == 777005 } == 1 }
                    check(system.activeNotifications.single { it.id == 777005 }.tag != null)
                }
            }
            test("typed SMS cancellation preserves an unrelated legacy budget") {
                withUnusedId(system, 777006) {
                    system.notify(777006, android.app.Notification.Builder(targetContext, NotificationManager.CHANNEL_BUDGET)
                        .setSmallIcon(targetContext.applicationInfo.icon).setContentTitle("QA legacy budget").build())
                    publisher.showStickyNotification(777006, "QA SMS removed", "Synthetic identity probe", 42)
                    awaitCondition { system.activeNotifications.count { it.id == 777006 } == 2 }
                    publisher.cancelNotification(777006, NotificationManager.CHANNEL_SMS, 42)
                    awaitCondition { system.activeNotifications.count { it.id == 777006 } == 1 }
                    check(system.activeNotifications.single { it.id == 777006 }.tag == null)
                }
            }
        }
        test("committing a real SMS transaction removes only its own card") {
            val drafts = getKoin().get<SmsDraftRepository>()
            val transactions = getKoin().get<TransactionRepository>()
            val source = runBlocking { transactions.getSourceById(3) } ?: error("Seed source 3 before the financial probe")
            check(source.currencyCode == "IRT") { "Financial fixture requires the IRT baseline" }
            val category = runBlocking { transactions.getCategoryById(6) } ?: error("Seed expense category 6 before the financial probe")
            check(category.type == TransactionType.EXPENSE)
            val count = runBlocking { transactions.getTransactionCount() }
            var draftId = -1L
            for (attempt in 0 until 20) {
                val candidate = runBlocking { drafts.addSmsDraft(SmsDraft(sender = "QA identity commit",
                    body = "بلوبانک برداشت 120,000 ریال کارت 2222", amount = 120000, bankName = "Blu",
                    type = TransactionType.EXPENSE, timeStamp = System.currentTimeMillis(), date = "1405/08/26",
                    sourceId = 3, categoryId = 6)) }
                if (system.activeNotifications.none { it.id == candidate.toInt() }) { draftId = candidate; break }
                runBlocking { drafts.deleteSmsDraft(candidate) }
            }
            check(draftId > 0) { "Could not allocate a fixture ID without touching existing cards" }
            try {
                withUnusedId(system, draftId.toInt()) {
                    publisher.showStickyNotification(draftId.toInt(), "QA committed SMS", "Synthetic identity probe", draftId)
                    publisher.showNotification(draftId.toInt(), "QA commit budget stays", "Synthetic identity probe", NotificationManager.CHANNEL_BUDGET, 1236)
                    awaitCondition { system.activeNotifications.count { it.id == draftId.toInt() } == 2 }
                    val result = runBlocking { getKoin().get<AddTransactionUseCase>()(Transaction(id = 0, amount = 12000,
                        currencyCode = "IRT", sourceId = 3, categoryId = 6, type = TransactionType.EXPENSE,
                        description = "QA notification identity committed SMS", date = "1405/08/26"),
                        emptyList(), emptyList(), smsDraftId = draftId) }
                    check(result > 0)
                    check(runBlocking { transactions.getTransactionCount() } == count + 1)
                    check(runBlocking { transactions.getSourceById(3) }!!.balance == source.balance - 12000)
                    check(runBlocking { drafts.getSmsDraftById(draftId) }!!.isUsed)
                    awaitCondition { system.activeNotifications.count { it.id == draftId.toInt() } == 1 }
                    check(system.activeNotifications.single { it.id == draftId.toInt() }.notification.extras
                        .getCharSequence(android.app.Notification.EXTRA_TITLE).toString() == "QA commit budget stays")
                }
            } finally { runBlocking { drafts.deleteSmsDraft(draftId) } }
        }
        finish(if (failures.isEmpty()) -1 else 0, Bundle().apply {
            putInt("tests", tests)
            putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n"))
        })
    }

    private fun withUnusedId(system: SystemNotificationManager, id: Int, action: () -> Unit) {
        check(system.activeNotifications.none { it.id == id }) { "Fixture ID $id is already in use; original cards were preserved" }
        try { action() } finally {
            system.activeNotifications.filter { it.id == id }.forEach { system.cancel(it.tag, it.id) }
        }
    }

    private fun awaitCard(system: SystemNotificationManager, id: Int): StatusBarNotification {
        repeat(20) {
            system.activeNotifications.firstOrNull { it.id == id }?.let { return it }
            Thread.sleep(100)
        }
        error("Notification $id was not delivered")
    }

    private fun awaitCondition(condition: () -> Boolean) {
        repeat(50) {
            if (condition()) return
            Thread.sleep(100)
        }
        error("Timed out waiting for the notification or draft state")
    }
}
