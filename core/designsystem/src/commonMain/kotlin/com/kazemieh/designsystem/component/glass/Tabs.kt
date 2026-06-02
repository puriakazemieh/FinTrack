package com.kazemieh.designsystem.component.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.GlassText3

/**
 * 2.8 Tabs — segmented tab control
 */
@Composable
fun Tabs(
    tabs: List<String>,
    active: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    counts: List<Int>? = null
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        padding = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isActive = index == active
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent,
                    animationSpec = tween(200)
                )
                val textColor by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onChange(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.W700 else FontWeight.W500,
                            color = textColor
                        )
                        
                        counts?.getOrNull(index)?.let { count ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = count.toLong().toFa(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.W700,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
