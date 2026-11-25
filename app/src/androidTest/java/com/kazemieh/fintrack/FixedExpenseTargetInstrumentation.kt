package com.kazemieh.fintrack

import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FintrackTheme
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseBottomSheet
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseIntent
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseViewModel
import java.io.File
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

/** Emulator-only form loading regression probe. Does not register transactions. */
class FixedExpenseTargetInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val failures = mutableListOf<String>()
        var tests = 0
        var ownedId: Long? = null
        var cases: FixedExpenseUseCaseGroup? = null
        val missingId = Long.MAX_VALUE
        val title = "QA-FIXED-TARGET-${System.currentTimeMillis()}"
        val output = File(targetContext.filesDir, "qa-fixed-target").apply { mkdirs() }
        try {
            check(Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish"))
            var repository: FixedExpenseRepository? = null
            repeat(100) {
                if (repository == null) repository = runCatching { getKoin().get<FixedExpenseRepository>() }.getOrNull()
                if (repository == null) Thread.sleep(100)
            }
            val records = checkNotNull(repository)
            cases = getKoin().get<FixedExpenseUseCaseGroup>()
            val category = runBlocking { getKoin().get<GetDefaultCategoryUseCase>()(TransactionType.EXPENSE).id }!!
            val source = runBlocking { getKoin().get<GetDefaultFinancialSourceUseCase>()()?.id }!!
            val now = System.currentTimeMillis()
            ownedId = runBlocking { records.insertFixedExpense(FixedExpense(title = title, amount = 1000L,
                categoryId = category, sourceId = source, recurrence = RecurrenceType.MONTHLY,
                startDate = now, nextDueDate = now + 86400000L, reminderEnabled = false)) }
            val id = ownedId!!
            val original = runBlocking { records.getFixedExpenseById(id)!! }
            lateinit var vm: AddFixedExpenseViewModel
            runOnMainSync { vm = getKoin().get<AddFixedExpenseViewModel>() }
            fun await(description: String, condition: () -> Boolean) {
                repeat(60) { if (condition()) return; Thread.sleep(100) }
                error("Timed out: $description")
            }
            fun load() {
                runOnMainSync { vm.onIntent(AddFixedExpenseIntent.LoadExpense(id)) }
                await("owned record") { vm.state.value.expenseId == id && vm.state.value.title == title }
            }
            fun test(name: String, block: () -> Unit) {
                tests++
                try { block() } catch (error: Throwable) { failures += "$name: ${error.stackTraceToString()}" }
            }
            test("switching to a missing record immediately clears old form data") {
                load()
                runOnMainSync { vm.onIntent(AddFixedExpenseIntent.LoadExpense(missingId)) }
                check(vm.state.value.expenseId == missingId && vm.state.value.title.isEmpty() && vm.state.value.amount.isEmpty()) {
                    "Old expense remains visible during another load"
                }
            }
            test("unavailable form ignores editing and cannot save or register") {
                runOnMainSync { vm.onIntent(AddFixedExpenseIntent.LoadExpense(missingId)) }
                Thread.sleep(400)
                check(vm.state.value.expenseId == missingId && vm.state.value.title.isEmpty()) { "Missing target did not replace old state" }
                runOnMainSync {
                    vm.onIntent(AddFixedExpenseIntent.SetTitle("MUST NOT APPLY"))
                    vm.onIntent(AddFixedExpenseIntent.SetAmount("2000"))
                    vm.onIntent(AddFixedExpenseIntent.Submit)
                    vm.onIntent(AddFixedExpenseIntent.RegisterAsTransaction)
                }
                Thread.sleep(300)
                check(vm.state.value.title.isEmpty() && vm.state.value.amount.isEmpty()) { "Unavailable editor accepted changes" }
                check(runBlocking { records.getFixedExpenseById(id) } == original) { "Unavailable form wrote the old record" }
            }
            test("reset prevents an in-flight edit load from repopulating a new form") {
                runOnMainSync {
                    vm.onIntent(AddFixedExpenseIntent.LoadExpense(id))
                    vm.onIntent(AddFixedExpenseIntent.Reset)
                }
                Thread.sleep(500)
                check(vm.state.value.expenseId == null && vm.state.value.title.isEmpty()) { "Late load overwrote reset" }
            }
            test("a later missing target wins over an earlier valid request") {
                runOnMainSync {
                    vm.onIntent(AddFixedExpenseIntent.LoadExpense(id))
                    vm.onIntent(AddFixedExpenseIntent.LoadExpense(missingId))
                }
                Thread.sleep(500)
                check(vm.state.value.expenseId == missingId && vm.state.value.title.isEmpty()) { "Earlier record won the load race" }
            }
            val activity = startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
            fun render(target: Long) {
                runOnMainSync {
                    activity.setContent {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            FintrackTheme(theme = AppTheme.GLASS_DARK) {
                                Box { AddFixedExpenseBottomSheet(onDismiss = {}, expenseId = target, viewModel = vm) }
                            }
                        }
                    }
                }
            }
            test("missing record renders feedback with no stale inputs and disabled save") {
                render(missingId)
                await("missing record feedback") { texts().contains("هزینهٔ ثابت پیدا نشد") }
                capture(output, "missing")
                check(nodes().none { it.className?.toString() == "android.widget.EditText" }) { "Stale fields are still shown" }
                check(!enabledSave()) { "Save remains enabled for missing record" }
            }
            test("valid edit still loads after the unavailable target") {
                render(id)
                await("valid editor recovers") { nodes().any { it.text?.toString() == title } && enabledSave() }
                capture(output, "recovered")
                check(runBlocking { records.getFixedExpenseById(id) } == original)
            }
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finally { ownedId?.let { id -> runCatching { runBlocking { cases?.deleteFixedExpenseUseCase?.invoke(id) } } } }
        finish(if (failures.isEmpty() && tests == 6) -1 else 0, Bundle().apply {
            putInt("tests", tests); putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n")); putString("record_ids", ownedId?.toString())
        })
    }
    private fun nodes(): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo?) { if (node == null) return; result += node; for (i in 0 until node.childCount) visit(node.getChild(i)) }
        visit(uiAutomation.rootInActiveWindow)
        return result
    }
    private fun texts() = nodes().mapNotNull { it.text?.toString() }.joinToString("\n")
    private fun enabledSave(): Boolean {
        var node = nodes().firstOrNull { it.text?.toString() == "ذخیره" }
        repeat(8) { if (node?.isClickable == true) return node!!.isEnabled; node = node?.parent }
        return false
    }
    private fun capture(directory: File, name: String) {
        File(directory, "$name.txt").writeText(texts())
        File(directory, "$name.png").outputStream().use { uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
