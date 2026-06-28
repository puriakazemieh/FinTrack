package com.kazemieh.ai_insights.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.HeaderAction
import com.kazemieh.designsystem.component.EmptyList
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.common.Ext.toPersianNumber
import com.kazemieh.common.Ext.toPriceFormat
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.PieChart
import com.kazemieh.designsystem.component.PieChartItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAdvisorScreen(
    onBack: () -> Unit,
    onAddTransaction: () -> Unit = {},
    viewModel: AIAdvisorViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val glassColors = LocalGlassColors.current
    var selectedSuggestion by remember { mutableStateOf<InvestmentSuggestion?>(null) }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(Res.string.ai_advisor_title),
                sub = stringResource(Res.string.ai_advisor_sub),
                onBack = onBack,
                actions = listOf(
                    HeaderAction(
                        icon = Icons.Default.Refresh,
                        color = glassColors.tertiary,
                        onClick = { viewModel.onIntent(AIAdvisorIntent.Refresh) }
                    )
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = glassColors.tertiary)
                }
            } else if (state.isEmpty) {
                EmptyList(
                    modifier = Modifier.padding(padding),
                    title = stringResource(Res.string.ai_empty_title),
                    description = stringResource(Res.string.ai_empty_desc)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        AnalysisCard(state)
                    }

                    item {
                        Text(
                            text = stringResource(Res.string.ai_active_suggestions_label) + " · " + state.suggestions.size.toPersianNumber(),
                            style = MaterialTheme.typography.labelMedium,
                            color = glassColors.text2,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }

                    items(state.suggestions) { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            onClick = { selectedSuggestion = suggestion }
                        )
                    }
                }
            }

            FAB(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                label = stringResource(Res.string.add_transaction),
                onClick = onAddTransaction
            )

            if (selectedSuggestion != null) {
                DetailBottomSheet(
                    suggestion = selectedSuggestion!!,
                    onDismiss = { selectedSuggestion = null }
                )
            }
        }
    }
}

@Composable
private fun AnalysisCard(state: AIAdvisorState) {
    val glassColors = LocalGlassColors.current

    val animatedPercentage = remember { Animatable(0f) }
    LaunchedEffect(state.savingPotentialPercentage) {
        animatedPercentage.animateTo(
            targetValue = state.savingPotentialPercentage.toFloat(),
            animationSpec = tween(durationMillis = 1500)
        )
    }

    GlassCard(
        tone = GlassTone.Strong,
        padding = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        0f to glassColors.tertiary.copy(alpha = 0.16f),
                        0.7f to Color.Transparent
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(glassColors.tertiary, glassColors.tertiary.copy(alpha = 0.8f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_92), // Spark icon
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.ai_analysis_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.text
                        )
                        Text(
                            text = stringResource(Res.string.ai_analysis_days, state.activeDays.toPersianNumber()),
                            style = MaterialTheme.typography.labelSmall,
                            color = glassColors.text2
                        )
                    }

                    // Simple Pie/Donut Chart
                    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        PieChart(
                            data = listOf(
                                PieChartItem(
                                    label = "",
                                    value = state.savingPotentialPercentage.toLong(),
                                    color = glassColors.tertiary
                                ),
                                PieChartItem(
                                    label = "",
                                    value = (100 - state.savingPotentialPercentage).toLong(),
                                    color = glassColors.glassHairline
                                )
                            ),
                            radiusOuter = 24.dp,
                            chartBarWidth = 6.dp,
                            textDistanceExtra = 0.dp,
                            showLegend = false,
                            enableAnimation = true
                        )
                        Text(
                            text = "٪${animatedPercentage.value.toInt().toPersianNumber()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(
                        Res.string.ai_analysis_body,
                        "٪${state.savingPotentialPercentage.toPersianNumber()}",
                        state.savingPotentialAmount.toPriceFormat().toPersianNumber()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassColors.text,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: InvestmentSuggestion,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val color = Color(suggestion.colorHex)

    GlassCard(
        padding = 0.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background radial glow equivalent
            Box(
                modifier = Modifier
                    .offset(x = (-30).dp, y = (-30).dp)
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(suggestion.icon),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(suggestion.title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(suggestion.body),
                            style = MaterialTheme.typography.labelSmall,
                            color = glassColors.text2,
                            lineHeight = 18.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = glassColors.text3,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = glassColors.glassHairline)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(
                        label = stringResource(Res.string.ai_suggestion_risk, stringResource(suggestion.risk.toStringResource())),
                        color = color
                    )
                    InfoChip(
                        label = stringResource(Res.string.ai_suggestion_return, stringResource(suggestion.returnRate).toPersianNumber()),
                        color = color
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBottomSheet(
    suggestion: InvestmentSuggestion,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val color = Color(suggestion.colorHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = glassColors.surface,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = glassColors.glassHairline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(suggestion.icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = stringResource(suggestion.title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = glassColors.text
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip(
                    label = stringResource(Res.string.ai_suggestion_risk, stringResource(suggestion.risk.toStringResource())),
                    color = color
                )
                InfoChip(
                    label = stringResource(Res.string.ai_suggestion_return, stringResource(suggestion.returnRate).toPersianNumber()),
                    color = color
                )
            }

            HorizontalDivider(color = glassColors.glassHairline)

            Text(
                text = stringResource(suggestion.detail),
                style = MaterialTheme.typography.bodyLarge,
                color = glassColors.text,
                lineHeight = 28.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(Res.string.confirm))
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun RiskLevel.toStringResource() = when (this) {
    RiskLevel.Low -> Res.string.risk_low
    RiskLevel.Medium -> Res.string.risk_medium
    RiskLevel.High -> Res.string.risk_high
}
