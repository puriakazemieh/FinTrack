package com.kazemieh.fintrack

import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.calc_tax
import fintrack.core.designsystem.generated.resources.notif_budget_exceeded_desc
import fintrack.core.designsystem.generated.resources.percentage_format
import fintrack.core.designsystem.generated.resources.label_percentage_value
import fintrack.core.designsystem.generated.resources.label_percentage
import fintrack.core.designsystem.generated.resources.label_percentage_suffix
import fintrack.core.designsystem.generated.resources.calc_discount
import fintrack.core.designsystem.generated.resources.label_goal_summary
import fintrack.core.designsystem.generated.resources.ai_suggestion_concentration_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_budget_body
import fintrack.core.designsystem.generated.resources.installment_reminder_message
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

/** Opt-in formatter probe; does not publish notifications or write financial data. */
class TranslationFormatInstrumentation : Instrumentation() {
    private var language = ""

    override fun onCreate(arguments: Bundle?) {
        language = arguments?.getString("language").orEmpty()
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val failures = mutableListOf<String>()
        var tests = 0
        try {
            check(android.os.Build.HARDWARE in setOf("ranchu", "goldfish"))
            check(language in setOf("fa", "en", "de")) { "Explicit language required" }
            startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            Thread.sleep(2_000)
            fun test(name: String, block: () -> Unit) {
                tests++
                runCatching(block).onFailure { failures += "$name: ${it.message}" }
            }
            test("budget percentage and positional arguments") {
                val message = runBlocking { getString(Res.string.notif_budget_exceeded_desc, "QA-CATEGORY", 87) }
                val prefix = when (language) { "en" -> "You have used"; "de" -> "Sie haben"; else -> "شما" }
                check(message.startsWith(prefix)) { "Wrong locale: $message" }
                check(message.contains("QA-CATEGORY") && message.contains("87")) { message }
                if (language == "fa") check(message.contains("درصد")) { message }
                else check(message.count { it == '%' } == 1) { "Expected one percent: $message" }
            }
            test("calculator tax percentage") {
                val text = runBlocking { getString(Res.string.calc_tax, "12") }
                val word = when (language) { "en" -> "tax"; "de" -> "Steuer"; else -> "مالیات" }
                check(text.contains(word)) { "Wrong locale: $text" }
                check(text.contains("12") && text.count { it == '%' || it == '٪' } == 1) { "Expected one percent: $text" }
            }
            test("shared percentage resources") {
                val texts = runBlocking { listOf(
                    getString(Res.string.percentage_format, "12"),
                    getString(Res.string.label_percentage_value, "12"),
                    getString(Res.string.label_percentage, "12"),
                    getString(Res.string.label_percentage_suffix, "QA-LABEL", "12"),
                    getString(Res.string.calc_discount, "12"),
                    getString(Res.string.label_goal_summary, "9000", 87),
                    getString(Res.string.ai_suggestion_concentration_body, "QA-CATEGORY", 87),
                    getString(Res.string.ai_suggestion_budget_body, "QA-CATEGORY", 87)
                ) }
                texts.forEachIndexed { index, text ->
                    check(text.count { it == '%' || it == '٪' } == 1) { "Format $index: $text" }
                    check(text.contains(if (index < 5) "12" else "87")) { "Missing argument $index: $text" }
                }
            }
            test("installment date argument") {
                val message = runBlocking { getString(Res.string.installment_reminder_message, "QA-DATE") }
                check(message.contains("QA-DATE") && '%' !in message) { message }
            }
        } catch (error: Throwable) { failures += "setup: ${error.stackTraceToString()}" }
        finish(if (tests == 4 && failures.isEmpty()) -1 else 0, Bundle().apply {
            putInt("tests", tests)
            putInt("failures", failures.size)
            putString("failure_details", failures.joinToString("\n"))
        })
    }
}
