package com.kazemieh.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.kazemieh.common.model.ToolFeature
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Single source of truth for how a [ToolFeature] is labeled and iconified, shared by
 * the Tools screen and the "manage features" settings screen so both stay in sync.
 */
@Composable
fun ToolFeature.displayLabel(): String = when (this) {
    ToolFeature.SOURCES -> stringResource(Res.string.financial_sources)
    ToolFeature.CATEGORIES -> stringResource(Res.string.category)
    ToolFeature.TAGS -> stringResource(Res.string.tags)
    ToolFeature.PERSONS -> stringResource(Res.string.persons)
    ToolFeature.BUDGETS -> stringResource(Res.string.label_budgets)
    ToolFeature.GOALS -> stringResource(Res.string.title_savings_goals)
    ToolFeature.FIXED_EXPENSE -> stringResource(Res.string.title_fixed_expense_management)
    ToolFeature.SHOPPING -> stringResource(Res.string.shopping_list)
    ToolFeature.NOTES -> stringResource(Res.string.notes)
    ToolFeature.INSTALLMENT -> stringResource(Res.string.navigation_installment)
    ToolFeature.DEBT -> stringResource(Res.string.navigation_debts)
    ToolFeature.CHECK -> stringResource(Res.string.title_check_management)
    ToolFeature.ASSETS -> stringResource(Res.string.title_assets_management)
    ToolFeature.FX_RATES -> stringResource(Res.string.title_fx_rates)
    ToolFeature.CONVERTER -> stringResource(Res.string.title_currency_converter)
    ToolFeature.NEWS -> stringResource(Res.string.title_news)
    ToolFeature.AI_ADVISOR -> stringResource(Res.string.ai_advisor_title)
    ToolFeature.ACHIEVEMENTS -> stringResource(Res.string.achievements_title)
    ToolFeature.EVENTS -> stringResource(Res.string.title_events)
    ToolFeature.FINANCIAL_CALENDAR -> stringResource(Res.string.financial_calendar_title)
    ToolFeature.FAQ -> stringResource(Res.string.title_faq)
    ToolFeature.SUPPORT -> stringResource(Res.string.title_support)
}

val ToolFeature.displayIcon: ImageVector
    get() = when (this) {
        ToolFeature.SOURCES -> Icons.Default.Wallet
        ToolFeature.CATEGORIES -> Icons.Default.Category
        ToolFeature.TAGS -> Icons.Default.Tag
        ToolFeature.PERSONS -> Icons.Default.Person
        ToolFeature.BUDGETS -> Icons.Default.PieChart
        ToolFeature.GOALS -> Icons.Default.TrackChanges
        ToolFeature.FIXED_EXPENSE -> Icons.Default.Autorenew
        ToolFeature.SHOPPING -> Icons.Default.ShoppingCart
        ToolFeature.NOTES -> Icons.AutoMirrored.Filled.Note
        ToolFeature.INSTALLMENT -> Icons.AutoMirrored.Filled.ReceiptLong
        ToolFeature.DEBT -> Icons.Default.Handshake
        ToolFeature.CHECK -> Icons.Default.Description
        ToolFeature.ASSETS -> Icons.Default.AccountBalance
        ToolFeature.FX_RATES -> Icons.Default.Language
        ToolFeature.CONVERTER -> Icons.Default.Transform
        ToolFeature.NEWS -> Icons.Default.Newspaper
        ToolFeature.AI_ADVISOR -> Icons.Default.AutoAwesome
        ToolFeature.ACHIEVEMENTS -> Icons.Default.EmojiEvents
        ToolFeature.EVENTS -> Icons.Default.Event
        ToolFeature.FINANCIAL_CALENDAR -> Icons.Default.CalendarMonth
        ToolFeature.FAQ -> Icons.Default.Quiz
        ToolFeature.SUPPORT -> Icons.Default.SupportAgent
    }
