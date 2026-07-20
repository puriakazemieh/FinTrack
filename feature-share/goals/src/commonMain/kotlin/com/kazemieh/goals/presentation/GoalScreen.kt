package com.kazemieh.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalHapticFeedback
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.designsystem.GlassGold
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import com.kazemieh.designsystem.component.glass.SheetFrame
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import com.kazemieh.goals.presentation.add.AddGoalBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalScreen(
    viewModel: GoalViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current
    val haptics = LocalHapticFeedback.current

    var showAddAmountDialog by remember { mutableStateOf(false) }
    var selectedGoalId by remember { mutableLongStateOf(0L) }
    var amountToAdd by remember { mutableStateOf("") }

    FintrackScreen(
        title = stringResource(Res.string.title_savings_goals),
        sub = stringResource(Res.string.label_active_goals, state.goals.size),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoalTabs(
                selected = state.currentTab,
                onSelect = { viewModel.onIntent(GoalIntent.SelectTab(it)) }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(space.medium),
                verticalArrangement = Arrangement.spacedBy(space.medium)
            ) {
                when (state.currentTab) {
                    GoalTab.GOALS -> {
                        item {
                            GoalSummaryHero(state)
                        }

                        item {
                            RoundUpSummaryCard(
                                state = state,
                                onClick = { viewModel.onIntent(GoalIntent.ToggleRoundUpSettings) }
                            )
                        }

                        item {
                            SearchBar(
                                query = state.searchQuery,
                                onQueryChange = { viewModel.onIntent(GoalIntent.UpdateSearchQuery(it)) },
                                placeholder = stringResource(Res.string.hint_search_in, stringResource(Res.string.title_savings_goals))
                            )
                        }

                        items(state.goals) { goal ->
                            GoalCard(
                                goal = goal,
                                onAddAmount = {
                                    selectedGoalId = goal.id
                                    amountToAdd = ""
                                    showAddAmountDialog = true
                                },
                                onEdit = { viewModel.onIntent(GoalIntent.ShowAddGoal(goal)) }
                            )
                        }
                    }

                    GoalTab.ROADMAP -> {
                        item {
                            FreedomRoadmap(state)
                        }
                    }

                    GoalTab.BASKET -> {
                        item {
                            FinancialBasketView(
                                state = state,
                                onAddBasket = { viewModel.onIntent(GoalIntent.AddBasket(it)) },
                                onUpdateBasket = { viewModel.onIntent(GoalIntent.UpdateBasket(it)) },
                                onDeleteBasket = { viewModel.onIntent(GoalIntent.DeleteBasket(it)) }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }

        FAB(onClick = { viewModel.onIntent(GoalIntent.ShowAddGoal()) })

        if (state.isAddGoalShow) {
            AddGoalBottomSheet(
                goal = state.selectedGoal,
                onDismiss = { viewModel.onIntent(GoalIntent.ShowAddGoal()) }
            )
        }

        if (showAddAmountDialog) {
            AddAmountDialog(
                onDismiss = { showAddAmountDialog = false },
                onConfirm = {
                    amountToAdd.toLongOrNull()?.let {
                        viewModel.onIntent(GoalIntent.AddAmountToGoal(selectedGoalId, it))
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    showAddAmountDialog = false
                },
                amountValue = amountToAdd,
                onAmountChange = { amountToAdd = it }
            )
        }

        if (state.showRoundUpSettings) {
            RoundUpSettingsSheet(
                state = state,
                onDismiss = { viewModel.onIntent(GoalIntent.ToggleRoundUpSettings) },
                onToggleEnabled = { viewModel.onIntent(GoalIntent.ToggleRoundUpEnabled) },
                onSelectGoal = { viewModel.onIntent(GoalIntent.SetRoundUpGoal(it)) },
                onSelectUnit = { viewModel.onIntent(GoalIntent.SetRoundUpUnit(it)) }
            )
        }
    }
}

@Composable
private fun RoundUpSummaryCard(state: GoalState, onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    val goalName = state.goals.find { it.id == state.roundUpGoalId }?.name
    val subtitle = when {
        !state.isRoundUpEnabled -> stringResource(Res.string.label_roundup_off)
        goalName != null -> stringResource(Res.string.label_roundup_unit_amount, state.roundUpUnit.toString().toPersianDigits()) + " · " + goalName
        else -> stringResource(Res.string.label_roundup_no_goal)
    }

    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = if (state.isRoundUpEnabled) GlassGreen else glassColors.text3
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_title))
                FintrackBodyMediumText(text = subtitle, color = glassColors.text3)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundUpSettingsSheet(
    state: GoalState,
    onDismiss: () -> Unit,
    onToggleEnabled: () -> Unit,
    onSelectGoal: (Long) -> Unit,
    onSelectUnit: (Long) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    val units = listOf(1_000L, 5_000L, 10_000L)

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = glassColors.bg0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = space.large)
                .padding(bottom = space.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_title), fontWeight = FontWeight.Bold)
                    FintrackBodyMediumText(text = stringResource(Res.string.label_roundup_desc), color = glassColors.text3)
                }
                com.kazemieh.designsystem.component.glass.Switch(on = state.isRoundUpEnabled, onToggle = { onToggleEnabled() })
            }

            if (state.isRoundUpEnabled) {
                Spacer(Modifier.height(space.medium))
                FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_target_goal), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (state.goals.isEmpty()) {
                    FintrackBodyMediumText(text = stringResource(Res.string.label_roundup_no_goals_yet), color = glassColors.text3)
                } else {
                    state.goals.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onSelectGoal(goal.id) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.roundUpGoalId == goal.id,
                                onClick = { onSelectGoal(goal.id) },
                                colors = RadioButtonDefaults.colors(selectedColor = GlassGreen)
                            )
                            FintrackBodyMediumText(text = goal.name)
                        }
                    }
                }

                Spacer(Modifier.height(space.medium))
                FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_unit), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    units.forEach { unit ->
                        val selected = state.roundUpUnit == unit
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(if (selected) GlassGreen.copy(alpha = 0.16f) else glassColors.glass)
                                .border(
                                    1.dp,
                                    if (selected) GlassGreen else glassColors.glassEdge,
                                    MaterialTheme.shapes.medium
                                )
                                .clickable { onSelectUnit(unit) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.label_roundup_unit_amount, unit.toString().toPersianDigits()),
                                color = if (selected) GlassGreen else glassColors.text2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalTabs(
    selected: GoalTab,
    onSelect: (GoalTab) -> Unit
) {
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = space.medium, vertical = space.small),
        horizontalArrangement = Arrangement.spacedBy(space.small)
    ) {
        GoalTabItem(
            label = stringResource(Res.string.title_savings_goals),
            selected = selected == GoalTab.GOALS,
            onClick = { onSelect(GoalTab.GOALS) },
            modifier = Modifier.weight(1f)
        )
        GoalTabItem(
            label = stringResource(Res.string.label_roadmap),
            selected = selected == GoalTab.ROADMAP,
            onClick = { onSelect(GoalTab.ROADMAP) },
            modifier = Modifier.weight(1f)
        )
        GoalTabItem(
            label = stringResource(Res.string.label_financial_basket),
            selected = selected == GoalTab.BASKET,
            onClick = { onSelect(GoalTab.BASKET) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GoalTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else glassColors.glass)
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else glassColors.glassEdge,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        FintrackLabelMediumText(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else glassColors.text2,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FreedomRoadmap(state: GoalState) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    
    val stages = listOf(
        Res.string.freedom_stage_1,
        Res.string.freedom_stage_2,
        Res.string.freedom_stage_3,
        Res.string.freedom_stage_4,
        Res.string.freedom_stage_5,
        Res.string.freedom_stage_6,
        Res.string.freedom_stage_7
    )

    Column(verticalArrangement = Arrangement.spacedBy(space.medium)) {
        FintrackTitleMediumText(
            text = stringResource(Res.string.label_roadmap),
            fontWeight = FontWeight.Bold
        )
        
        stages.forEachIndexed { index, stageRes ->
            val level = index + 1
            val isCompleted = state.freedomStage.level > level
            val isCurrent = state.freedomStage.level == level
            
            RoadmapStageCard(
                stage = stringResource(stageRes),
                step = level,
                isCompleted = isCompleted,
                isCurrent = isCurrent
            )
        }
    }
}

@Composable
private fun RoadmapStageCard(
    stage: String,
    step: Int,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val glassColors = LocalGlassColors.current
    val color = when {
        isCompleted -> GlassGreen
        isCurrent -> MaterialTheme.colorScheme.primary
        else -> glassColors.text3
    }

    GlassCard(
        tone = if (isCurrent) GlassTone.Strong else GlassTone.Default
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .border(1.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FintrackLabelMediumText(text = step.toString().toPersianDigits(), color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            FintrackBodyMediumText(
                text = stage,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) glassColors.text else glassColors.text2
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GlassGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FinancialBasketView(
    state: GoalState,
    onAddBasket: (com.kazemieh.common.model.GoalBasket) -> Unit,
    onUpdateBasket: (com.kazemieh.common.model.GoalBasket) -> Unit,
    onDeleteBasket: (Long) -> Unit
) {
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current
    var showManageBaskets by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(space.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackTitleMediumText(
                text = stringResource(Res.string.label_financial_basket),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showManageBaskets = true }) {
                Icon(Icons.Default.Settings, null, tint = glassColors.text3)
            }
        }
        
        state.basketItems.forEach { item ->
            BasketCard(
                title = item.name,
                amount = item.amount,
                percent = item.percent,
                color = when(item.colorId) {
                    1 -> GlassGreen
                    2 -> MaterialTheme.colorScheme.primary
                    3 -> GlassGold
                    else -> glassColors.text3
                }
            )
        }
    }

    if (showManageBaskets) {
        ManageBasketsBottomSheet(
            baskets = state.baskets,
            onAdd = onAddBasket,
            onUpdate = onUpdateBasket,
            onDelete = onDeleteBasket,
            onDismiss = { showManageBaskets = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageBasketsBottomSheet(
    baskets: List<com.kazemieh.common.model.GoalBasket>,
    onAdd: (com.kazemieh.common.model.GoalBasket) -> Unit,
    onUpdate: (com.kazemieh.common.model.GoalBasket) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBasket by remember { mutableStateOf<com.kazemieh.common.model.GoalBasket?>(null) }
    var name by remember { mutableStateOf("") }

    com.kazemieh.designsystem.component.glass.SheetFrame(
        title = stringResource(Res.string.label_financial_basket),
        onDismiss = onDismiss,
        primaryButtonText = stringResource(Res.string.add_new_item),
        onPrimaryClick = {
            editingBasket = null
            name = ""
            showAddDialog = true
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            baskets.forEach { basket ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalGlassColors.current.glass)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FintrackBodyMediumText(text = basket.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        editingBasket = basket
                        name = basket.name
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Edit, null, tint = LocalGlassColors.current.text3, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onDelete(basket.id) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotEmpty()) {
                        if (editingBasket == null) {
                            onAdd(com.kazemieh.common.model.GoalBasket(name = name))
                        } else {
                            onUpdate(editingBasket!!.copy(name = name))
                        }
                        showAddDialog = false
                    }
                }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(Res.string.cancell_))
                }
            },
            title = { Text(if (editingBasket == null) stringResource(Res.string.add_new_item) else stringResource(Res.string.edit)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.label_title)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

@Composable
private fun BasketCard(
    title: String,
    amount: Long,
    percent: Int,
    color: Color
) {
    val glassColors = LocalGlassColors.current
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FintrackBodyMediumText(text = title, fontWeight = FontWeight.Bold)
                FintrackLabelMediumText(text = "${percent}%".toPersianDigits(), color = color, fontWeight = FontWeight.Bold)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(glassColors.glassHairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
            
            FintrackTitleMediumText(text = amount.toPersianPrice(), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun GoalSummaryHero(state: GoalState) {
    val glassColors = LocalGlassColors.current
    val color = MaterialTheme.colorScheme.primary

    GlassCard(
        tone = GlassTone.Strong,
        padding = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        0f to color.copy(alpha = 0.16f),
                        0.7f to Color.Transparent
                    )
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.label_total_saved),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3
                )
                Text(
                    text = state.totalSavedAmount.toPersianPrice(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.text
                )
                Text(
                    text = stringResource(
                        Res.string.label_goal_summary,
                        state.totalTargetAmount.toPersianPrice(),
                        state.totalPercent
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: com.kazemieh.common.model.Goal,
    onAddAmount: () -> Unit,
    onEdit: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        padding = 14.dp,
        onClick = onEdit
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.text
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FintrackLabelSmallText(
                            text = when(goal.category) {
                                com.kazemieh.common.model.GoalCategory.SHORT_TERM -> stringResource(Res.string.category_short)
                                com.kazemieh.common.model.GoalCategory.MID_TERM -> stringResource(Res.string.category_mid)
                                com.kazemieh.common.model.GoalCategory.LONG_TERM -> stringResource(Res.string.category_long)
                            },
                            color = glassColors.text3
                        )
                        FintrackLabelSmallText(text = "·", color = glassColors.text3)
                        FintrackLabelSmallText(
                            text = when(goal.priority) {
                                com.kazemieh.common.model.GoalPriority.LOW -> stringResource(Res.string.priority_low)
                                com.kazemieh.common.model.GoalPriority.MEDIUM -> stringResource(Res.string.priority_medium)
                                com.kazemieh.common.model.GoalPriority.HIGH -> stringResource(Res.string.priority_high_goal)
                            },
                            color = when(goal.priority) {
                                com.kazemieh.common.model.GoalPriority.HIGH -> Color.Red
                                else -> glassColors.text3
                            }
                        )
                    }
                }

                Text(
                    text = "${goal.percent}%".toPersianDigits(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (goal.description.isNotEmpty()) {
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(glassColors.glassHairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.percent / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.savedAmount.toPersianPrice(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = goal.targetAmount.toPersianPrice() + " " + stringResource(Res.string.unit_toman_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onAddAmount,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "+ " + stringResource(Res.string.label_add_to_goal),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = glassColors.bg0,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            FintrackTitleMediumText(
                text = stringResource(Res.string.label_add_to_goal),
                fontWeight = FontWeight.Bold,
                color = glassColors.text
            )

            TextField(
                value = amountValue,
                onValueChange = onAmountChange,
                placeholder = {
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.amount),
                        color = glassColors.text3
                    )
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = GlassGreen
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = glassColors.text,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassColors.bg1)
                    .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space.medium)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glassColors.glass,
                        contentColor = glassColors.text
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(Res.string.cancell_), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(Res.string.confirm), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(space.medium))
        }
    }
}
