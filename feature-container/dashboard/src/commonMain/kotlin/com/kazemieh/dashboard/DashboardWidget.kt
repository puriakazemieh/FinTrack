package com.kazemieh.dashboard

import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.achievements_title
import fintrack.core.designsystem.generated.resources.ai_advisor_title
import fintrack.core.designsystem.generated.resources.label_budgets
import fintrack.core.designsystem.generated.resources.navigation_installment
import fintrack.core.designsystem.generated.resources.navigation_transactions
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.title_assets_management
import fintrack.core.designsystem.generated.resources.title_check_management
import fintrack.core.designsystem.generated.resources.title_fixed_expense_management
import fintrack.core.designsystem.generated.resources.title_savings_goals
import org.jetbrains.compose.resources.StringResource

/**
 * The reorderable / hideable content widgets on the dashboard. The header,
 * balance hero, SMS banner and quick actions are structural and always shown;
 * everything in this enum can be reordered or hidden by the user.
 */
enum class DashboardWidget(val label: StringResource) {
    RECENT_TRANSACTIONS(Res.string.navigation_transactions),
    ACHIEVEMENTS(Res.string.achievements_title),
    BUDGET(Res.string.label_budgets),
    GOAL(Res.string.title_savings_goals),
    INSTALLMENT(Res.string.navigation_installment),
    CHECK(Res.string.title_check_management),
    FIXED_EXPENSE(Res.string.title_fixed_expense_management),
    AI_ADVISOR(Res.string.ai_advisor_title),
    ASSET(Res.string.title_assets_management),
    SHOPPING(Res.string.shopping_list),
    NOTES(Res.string.notes);

    companion object {
        fun defaultConfig(): List<DashboardWidgetItem> = entries.map { DashboardWidgetItem(it, true) }

        /**
         * Parse the persisted layout. Each token is a widget name, prefixed with
         * "-" when hidden. Unknown tokens are dropped and widgets added in a newer
         * app version are appended (visible) so the layout stays forward-compatible.
         */
        fun parse(csv: String): List<DashboardWidgetItem> {
            if (csv.isBlank()) return defaultConfig()
            val parsed = csv.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { token ->
                    val visible = !token.startsWith("-")
                    val name = token.removePrefix("-")
                    entries.firstOrNull { it.name == name }?.let { DashboardWidgetItem(it, visible) }
                }
            if (parsed.isEmpty()) return defaultConfig()
            val known = parsed.map { it.widget }.toSet()
            val missing = entries.filter { it !in known }.map { DashboardWidgetItem(it, true) }
            return parsed + missing
        }

        fun serialize(items: List<DashboardWidgetItem>): String =
            items.joinToString(",") { (if (it.visible) "" else "-") + it.widget.name }
    }
}

/** A widget together with the user's visibility choice; order is list position. */
data class DashboardWidgetItem(
    val widget: DashboardWidget,
    val visible: Boolean
)
