package com.kazemieh.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGold
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassPurple
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.designsystem.component.glass.ToolEntry
import com.kazemieh.designsystem.component.glass.ToolGroupSection
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private data class ToolGroup(
    val title: String,
    val accent: androidx.compose.ui.graphics.Color,
    val items: List<ToolEntry>
)

@Composable
fun ToolsScreen(
    onNavigateToBudget: () -> Unit,
    onNavigateToGoal: () -> Unit,
    onNavigateToInstallment: () -> Unit,
    onNavigateToPerson: () -> Unit,
    onNavigateToDebt: () -> Unit,
    onNavigateToCheck: () -> Unit,
    onNavigateToFixedExpense: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToSource: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToTag: () -> Unit,
    onNavigateToAIAdvisor: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToFxRates: () -> Unit,
    onNavigateToCurrencyConverter: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToFinancialCalendar: () -> Unit
) {
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    // Individual tools, defined once and referenced from both their category and
    // the curated "frequently used" section.
    val sources = ToolEntry(stringResource(Res.string.financial_sources), Icons.Default.Wallet, onNavigateToSource)
    val categories = ToolEntry(stringResource(Res.string.category), Icons.Default.Category, onNavigateToCategory)
    val tags = ToolEntry(stringResource(Res.string.tags), Icons.Default.Tag, onNavigateToTag)
    val persons = ToolEntry(stringResource(Res.string.persons), Icons.Default.Person, onNavigateToPerson)
    val budgets = ToolEntry(stringResource(Res.string.label_budgets), Icons.Default.PieChart, onNavigateToBudget)
    val goals = ToolEntry(stringResource(Res.string.title_savings_goals), Icons.Default.TrackChanges, onNavigateToGoal)
    val fixedExpense = ToolEntry(stringResource(Res.string.title_fixed_expense_management), Icons.Default.Autorenew, onNavigateToFixedExpense)
    val shopping = ToolEntry(stringResource(Res.string.shopping_list), Icons.Default.ShoppingCart, onNavigateToShopping)
    val notes = ToolEntry(stringResource(Res.string.notes), Icons.AutoMirrored.Filled.Note, onNavigateToNotes)

    val installment = ToolEntry(stringResource(Res.string.navigation_installment), Icons.AutoMirrored.Filled.ReceiptLong, onNavigateToInstallment)
    val debt = ToolEntry(stringResource(Res.string.navigation_debts), Icons.Default.Handshake, onNavigateToDebt)
    val check = ToolEntry(stringResource(Res.string.title_check_management), Icons.Default.Description, onNavigateToCheck)

    val assets = ToolEntry(stringResource(Res.string.title_assets_management), Icons.Default.AccountBalance, onNavigateToAssets)
    val fxRates = ToolEntry(stringResource(Res.string.title_fx_rates), Icons.Default.Language, onNavigateToFxRates)
    val converter = ToolEntry(stringResource(Res.string.title_currency_converter), Icons.Default.Transform, onNavigateToCurrencyConverter)
    val news = ToolEntry(stringResource(Res.string.title_news), Icons.Default.Newspaper, onNavigateToNews)

    val aiAdvisor = ToolEntry(stringResource(Res.string.ai_advisor_title), Icons.Default.AutoAwesome, onNavigateToAIAdvisor)
    val achievements = ToolEntry(stringResource(Res.string.achievements_title), Icons.Default.EmojiEvents, onNavigateToAchievements)
    val events = ToolEntry(stringResource(Res.string.title_events), Icons.Default.Event, onNavigateToEvents)
    val financialCalendar = ToolEntry(stringResource(Res.string.financial_calendar_title), Icons.Default.CalendarMonth, onNavigateToFinancialCalendar)

    val faq = ToolEntry(stringResource(Res.string.title_faq), Icons.Default.Quiz, onNavigateToFAQ)
    val support = ToolEntry(stringResource(Res.string.title_support), Icons.Default.SupportAgent, onNavigateToSupport)

    val favorites = ToolGroup(
        title = stringResource(Res.string.tools_group_favorites),
        accent = GlassGold,
        items = listOf(budgets, goals, sources, aiAdvisor, converter, shopping)
    )
    val groups = listOf(
        ToolGroup(
            title = stringResource(Res.string.tools_group_money),
            accent = GlassGreen,
            items = listOf(sources, categories, tags, persons, budgets, goals, fixedExpense, shopping, notes)
        ),
        ToolGroup(
            title = stringResource(Res.string.tools_group_debt),
            accent = GlassAmber,
            items = listOf(installment, debt, check)
        ),
        ToolGroup(
            title = stringResource(Res.string.tools_group_assets),
            accent = GlassBlue,
            items = listOf(assets, fxRates, converter, news)
        ),
        ToolGroup(
            title = stringResource(Res.string.tools_group_insights),
            accent = GlassPurple,
            items = listOf(aiAdvisor, achievements, events, financialCalendar)
        ),
        ToolGroup(
            title = stringResource(Res.string.tools_group_support),
            accent = glassColors.text3,
            items = listOf(faq, support)
        )
    )

    var query by remember { mutableStateOf("") }

    FintrackScreen(
        title = stringResource(Res.string.navigation_tools),
        sub = stringResource(Res.string.title_tools_management)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = space.large)
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(Res.string.tools_search_placeholder),
                modifier = Modifier.padding(vertical = space.medium)
            )

            if (query.isBlank()) {
                ToolGroupSection(
                    title = favorites.title,
                    accent = favorites.accent,
                    items = favorites.items
                )
                groups.forEach { group ->
                    ToolGroupSection(
                        title = group.title,
                        accent = group.accent,
                        items = group.items
                    )
                }
            } else {
                // Flatten + de-duplicate across groups, then filter by label.
                val results = groups
                    .flatMap { it.items }
                    .distinctBy { it.label }
                    .filter { it.label.contains(query, ignoreCase = true) }

                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.tools_no_results),
                            color = glassColors.text3
                        )
                    }
                } else {
                    ToolGroupSection(
                        title = stringResource(Res.string.navigation_tools),
                        accent = GlassGreen,
                        items = results
                    )
                }
            }

            Box(modifier = Modifier.padding(bottom = 100.dp))
        }
    }
}
