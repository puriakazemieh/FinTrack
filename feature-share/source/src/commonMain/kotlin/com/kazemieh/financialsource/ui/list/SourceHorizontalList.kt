package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.Source
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.LeadingIconStyle
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_new_source
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SourceHorizontalList(
    viewModel: SourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit,
    onSourceClick: (Source) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space.medium),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(state.sources) { source ->
            SourceTile(
                source = source,
                onClick = { onSourceClick(source) }
            )
        }
        item {
            NewSourceTile(onClick = onAddSourceClick)
        }
    }
}

@Composable
private fun SourceTile(
    source: Source,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    val color = when {
        source.balance < 0 -> GlassRed
        source.balance > 0 -> GlassGreen
        else -> GlassGreen
    }

    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FinTrackLeadingIcon(
                    colorId = source.colorId,
                    iconId = source.iconId,
                    style = LeadingIconStyle.Badge,
                    size = 18.dp,
                    iconSize = 10.dp,
                    corner = 6.dp
                )
                FintrackLabelSmallText(
                    text = source.name,
                    fontSize = 10.5.sp,
                    maxLines = 1
                )

            }
            FintrackTitleSmallText(
                text = if (LocalHideBalance.current) "••••" else source.balance.toSignedPersianPrice(),
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
                color = color
            )
        }
    }
}

@Composable
private fun NewSourceTile(onClick: () -> Unit) {
    val stroke = remember {
        Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    }
    val color = GlassGreen

    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GlassGreenSoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = color.copy(alpha = 0.45f),
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassGreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            FintrackLabelSmallText(
                text = stringResource(Res.string.label_new_source),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
