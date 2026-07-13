package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.common.DateFilterType
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_range
import fintrack.core.designsystem.generated.resources.label_this_year
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeriodNavigator(
    currentPeriod: DateFilterType,
    periodLabel: String,
    periodSubLabel: String,
    onPeriodSelected: (DateFilterType) -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTypes: Boolean = true,
    excludeCustomRange: Boolean = false
) {
    val periods = listOfNotNull(
        DateFilterType.TODAY to stringResource(Res.string.today),
        DateFilterType.THIS_WEEK to stringResource(Res.string.this_week),
        DateFilterType.THIS_MONTH to stringResource(Res.string.this_month),
        DateFilterType.THIS_YEAR to stringResource(Res.string.label_this_year),
        if (excludeCustomRange) null else DateFilterType.CUSTOM_RANGE to stringResource(Res.string.label_range)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 8.dp,
            tone = GlassTone.Default
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = LocalGlassColors.current.text2,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FintrackBodyLargeText(
                        text = periodLabel,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (periodSubLabel.isNotEmpty()) {
                        FintrackLabelSmallText(
                            text = periodSubLabel,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = LocalGlassColors.current.text2,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showTypes) {
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(periods) { (type, label) ->
                    val active = currentPeriod == type
                    Chip(
                        active = active,
                        color = GlassGreen,
                        onClick = { onPeriodSelected(type) }
                    ) {
                        FintrackLabelMediumText(
                            text = label,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) LocalGlassColors.current.bg0 else LocalGlassColors.current.text2
                        )
                    }
                }
            }
        }
    }
}
