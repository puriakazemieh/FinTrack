package com.kazemieh.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.financial_sources
import fintrack.core.designsystem.generated.resources.label_budgets
import fintrack.core.designsystem.generated.resources.navigation_debts
import fintrack.core.designsystem.generated.resources.navigation_installment
import fintrack.core.designsystem.generated.resources.navigation_tools
import fintrack.core.designsystem.generated.resources.notes
import fintrack.core.designsystem.generated.resources.persons
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_assets_management
import fintrack.core.designsystem.generated.resources.title_check_management
import fintrack.core.designsystem.generated.resources.title_fixed_expense_management
import fintrack.core.designsystem.generated.resources.title_tools_management
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToolsScreen(
    onNavigateToBudget: () -> Unit,
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
    onNavigateToAIAdvisor: () -> Unit
) {
    val space = LocalSpacing.current

    val tools = listOf(
        ToolItem(
            stringResource(Res.string.financial_sources),
            Icons.Default.Wallet,
            onNavigateToSource
        ),
        ToolItem(stringResource(Res.string.category), Icons.Default.Category, onNavigateToCategory),
        ToolItem(stringResource(Res.string.tags), Icons.Default.Tag, onNavigateToTag),
        ToolItem(stringResource(Res.string.persons), Icons.Default.Person, onNavigateToPerson),
        ToolItem(
            stringResource(Res.string.label_budgets),
            Icons.Default.PieChart,
            onNavigateToBudget
        ),
        ToolItem(
            stringResource(Res.string.navigation_installment),
            Icons.AutoMirrored.Filled.ReceiptLong,
            onNavigateToInstallment
        ),
        ToolItem(
            stringResource(Res.string.navigation_debts),
            Icons.Default.Handshake,
            onNavigateToDebt
        ),
        ToolItem(
            stringResource(Res.string.title_check_management),
            Icons.Default.Description,
            onNavigateToCheck
        ),
        ToolItem(
            stringResource(Res.string.title_fixed_expense_management),
            Icons.Default.Autorenew,
            onNavigateToFixedExpense
        ),
        ToolItem(
            stringResource(Res.string.ai_advisor_title),
            Icons.Default.AutoAwesome,
            onNavigateToAIAdvisor
        ),
        ToolItem(
            stringResource(Res.string.title_assets_management),
            Icons.Default.AccountBalance,
            onNavigateToAssets
        ),
        ToolItem(
            stringResource(Res.string.shopping_list),
            Icons.Default.ShoppingCart,
            onNavigateToShopping
        ),
        ToolItem(
            stringResource(Res.string.notes),
            Icons.AutoMirrored.Filled.Note,
            onNavigateToNotes
        )
    )

    FintrackScreen(
        title = stringResource(Res.string.navigation_tools),
        sub = stringResource(Res.string.title_tools_management)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(space.large),
            horizontalArrangement = Arrangement.spacedBy(space.medium),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            items(tools) { tool ->
                ToolCard(tool)
            }
        }
    }
}

@Composable
private fun ToolCard(tool: ToolItem) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f),
        onClick = tool.onClick,
        padding = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            FintrackLabelMediumText(
                text = tool.label,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

data class ToolItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
