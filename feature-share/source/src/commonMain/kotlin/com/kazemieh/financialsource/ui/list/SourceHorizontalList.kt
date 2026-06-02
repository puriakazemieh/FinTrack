package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.LeadingIconStyle
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
            .size(width = 110.dp, height = 70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(space.medium)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            FinTrackLeadingIcon(
                colorId = source.colorId,
                iconId = source.iconId,
                style = LeadingIconStyle.Badge,
                size = 28.dp,
                iconSize = 16.dp
            )
            Text(
                text = source.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NewSourceTile(onClick: () -> Unit) {
    val stroke = remember {
        Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    }
    val color = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 70.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(2.dp), // Space for dashed border
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = color,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
