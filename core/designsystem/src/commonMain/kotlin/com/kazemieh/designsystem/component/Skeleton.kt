package com.kazemieh.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing

/**
 * A shimmering placeholder block used while real content is loading. Reads its base
 * tint from [LocalGlassColors] so it matches both the glass and plain themes.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
) {
    val glassColors = LocalGlassColors.current
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonTranslate"
    )
    val base = glassColors.glassStrong
    val highlight = glassColors.glassHover
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate, 0f),
        end = Offset(translate + 300f, 300f)
    )
    Spacer(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/** A single skeleton row shaped like a transaction/entity list item. */
@Composable
fun SkeletonListRow(modifier: Modifier = Modifier) {
    val space = LocalSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = space.small),
        horizontalArrangement = Arrangement.spacedBy(space.small)
    ) {
        SkeletonBox(
            modifier = Modifier.size(44.dp),
            shape = androidx.compose.foundation.shape.CircleShape
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp))
        }
        SkeletonBox(modifier = Modifier.width(60.dp).height(20.dp))
    }
}

/** A vertical stack of [count] [SkeletonListRow]s, for list loading states. */
@Composable
fun SkeletonList(modifier: Modifier = Modifier, count: Int = 6) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(count) {
            SkeletonListRow()
        }
    }
}
