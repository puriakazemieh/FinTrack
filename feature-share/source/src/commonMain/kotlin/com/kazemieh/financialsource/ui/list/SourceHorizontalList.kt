package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.kazemieh.common.toFa
import com.kazemieh.common.formatted
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SourceHorizontalList(
    viewModel: SourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit,
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
            SourceTile(source = source)
        }
        item {
            NewSourceTile(onClick = onAddSourceClick)
        }
    }
}

@Composable
private fun SourceTile(source: Source) {
    val space = LocalSpacing.current
    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GlassColor)
            .border(1.dp, GlassEdge, RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                FintrackLabelSmallText(
                    text = source.name,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    color = GlassText2
                )
                FinTrackLeadingIcon(
                    colorId = source.colorId,
                    iconId = source.iconId,
                    style = LeadingIconStyle.Badge,
                    size = 18.dp,
                    iconSize = 10.dp,
                    corner = 6.dp
                )
            }
            FintrackTitleSmallText(
                text = source.balance.formatted(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlassText
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
