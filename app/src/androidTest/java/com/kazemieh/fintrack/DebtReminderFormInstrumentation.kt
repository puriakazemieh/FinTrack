package com.kazemieh.fintrack

import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kazemieh.common.model.DebtType
import com.kazemieh.debt.ui.add.AddDebtIntent
import com.kazemieh.debt.ui.add.AddDebtViewModel
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Opt-in emulator probe: load and description-only save through the real form. */
class DebtReminderFormInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val ids = mutableListOf<Long>()
        val failures = mutableListOf<String>()
        var tests = 0
        var cases: DebtUseCaseGroup? = null
        var scheduler: NotificationScheduler? = null
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish")) { "Emulator only" }
            var repository: DebtRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<DebtRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository)
            cases = getKoin().get<DebtUseCaseGroup>()
            scheduler = getKoin().get<NotificationScheduler>()
            val work = WorkManager.getInstance(targetContext)
            lateinit var vm: AddDebtViewModel
            runOnMainSync { vm = getKoin().get<AddDebtViewModel>() }
            for (type in DebtType.entries) for (enabled in listOf(true, false)) {
                tests++
                val name = "$type/$enabled"
                try {
                    // Existing BASE-01b fixtures supply real person/category/source relations.
                    val base = runBlocking { records.getDebtById(if (type == DebtType.OWED_BY_ME) 1 else 2) }!!
                    check(base.type == type && !base.isSettled)
                    val original = base.copy(id = 0, description = "QA-DEBT-FORM-${System.currentTimeMillis()}-$name",
                        dueDate = System.currentTimeMillis() + 86400000L, reminderEnabled = enabled)
                    val id = runBlocking { cases!!.addDebtUseCase(original) }
                    ids += id
                    val persisted = runBlocking { records.getDebtById(id) }!!
                    val originalTags = runBlocking { cases!!.getDebtWithRelationsUseCase(id) }!!.tags
                    if (enabled) check(work.getWorkInfosForUniqueWork("debt_$id").get().any { !it.state.isFinished })
                    runOnMainSync { vm.onIntent(AddDebtIntent.LoadDebt(id)) }
                    await("load $name") { vm.state.value.debtId == id }
                    val loadedCorrectly = vm.state.value.reminderEnabled == enabled
                    runOnMainSync {
                        vm.onIntent(AddDebtIntent.SetDescription(original.description + " edited"))
                        vm.onIntent(AddDebtIntent.Submit)
                    }
                    var saved = runBlocking { records.getDebtById(id) }!!
                    await("save $name") {
                        saved = runBlocking { records.getDebtById(id) }!!
                        saved.description == original.description + " edited"
                    }
                    check(loadedCorrectly) { "Loaded reminder flag differs from persisted $enabled" }
                    check(saved.reminderEnabled == enabled) { "Description-only save lost reminder" }
                    check(saved.copy(description = persisted.description, updatedAt = persisted.updatedAt) == persisted) {
                        "Other debt fields changed"
                    }
                    check(runBlocking { cases!!.getDebtWithRelationsUseCase(id) }!!.tags == originalTags)
                    await("work $name") {
                        val active = work.getWorkInfosForUniqueWork("debt_$id").get().filter { !it.state.isFinished }
                        if (enabled) active.size == 1 && active.single().state == WorkInfo.State.ENQUEUED else active.isEmpty()
                    }
                } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}" }
            }
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finally {
            ids.forEach { id ->
                runCatching { scheduler?.cancelReminder("debt_$id") }
                runCatching { runBlocking { cases?.deleteDebtUseCase?.invoke(id) } }
            }
        }
        finish(if (failures.isEmpty() && tests == 4) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n")); putString("record_ids", ids.joinToString(","))
        })
    }
    private fun await(name: String, condition: () -> Boolean) {
        repeat(100) { if (condition()) return; Thread.sleep(100) }
        error("Timed out: $name")
    }
}
