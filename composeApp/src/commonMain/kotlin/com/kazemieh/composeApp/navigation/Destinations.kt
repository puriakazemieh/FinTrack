package com.kazemieh.composeApp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.achievements_title
import fintrack.core.designsystem.generated.resources.ai_advisor_title
import fintrack.core.designsystem.generated.resources.label_budgets
import fintrack.core.designsystem.generated.resources.navigation_home
import fintrack.core.designsystem.generated.resources.navigation_profile
import fintrack.core.designsystem.generated.resources.navigation_tools
import fintrack.core.designsystem.generated.resources.navigation_transactions
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.title_assets_management
import fintrack.core.designsystem.generated.resources.title_savings_goals
import org.jetbrains.compose.resources.StringResource

/**
 * Destinations that may appear on the bottom navigation bar. The first four are the
 * historical defaults; the rest let the user swap any of these tools onto a tab slot.
 * [matchKey] is the route class' simple name, used to detect the active tab from the
 * current back-stack route. Every [route] here is registered in `bottomBarNavGraph`.
 */
enum class Destinations(
    val icon: ImageVector,
    val label: StringResource,
    val route: Screen,
    val matchKey: String
) {
    DASHBOARD(Icons.Filled.Home, Res.string.navigation_home, Screen.Dashboard(), "Dashboard"),
    TRANSACTIONS(Icons.AutoMirrored.Filled.ReceiptLong, Res.string.navigation_transactions, Screen.Transactions(), "Transactions"),
    TOOLS(Icons.Filled.Apps, Res.string.navigation_tools, Screen.Tools, "Tools"),
    PROFILE(Icons.Filled.Person, Res.string.navigation_profile, Screen.Profile, "Profile"),
    BUDGET(Icons.Filled.PieChart, Res.string.label_budgets, Screen.Budget, "Budget"),
    GOAL(Icons.Filled.TrackChanges, Res.string.title_savings_goals, Screen.Goal, "Goal"),
    ASSETS(Icons.Filled.AccountBalance, Res.string.title_assets_management, Screen.Assets, "Assets"),
    ACHIEVEMENTS(Icons.Filled.EmojiEvents, Res.string.achievements_title, Screen.Achievements, "Achievements"),
    NOTES(Icons.AutoMirrored.Filled.Note, Res.string.notes, Screen.Notes, "Notes"),
    SHOPPING(Icons.Filled.ShoppingCart, Res.string.shopping_list, Screen.Shopping, "Shopping"),
    AI_ADVISOR(Icons.Filled.AutoAwesome, Res.string.ai_advisor_title, Screen.AIAdvisor, "AIAdvisor");

    companion object {
        val Defaults = listOf(DASHBOARD, TRANSACTIONS, TOOLS, PROFILE)

        /** Parse a persisted CSV of tab names; falls back to defaults unless exactly 4 valid tabs. */
        fun parseTabs(csv: String): List<Destinations> {
            if (csv.isBlank()) return Defaults
            val tabs = csv.split(",")
                .mapNotNull { name -> entries.firstOrNull { it.name == name.trim() } }
                .distinct()
            return if (tabs.size == 4) tabs else Defaults
        }

        fun serializeTabs(tabs: List<Destinations>): String = tabs.joinToString(",") { it.name }
    }
}
