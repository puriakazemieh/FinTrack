package com.kazemieh.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FintrackSwipeableRow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    backgroundContent: @Composable (progress: Float) -> Unit,
    content: @Composable () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    var width by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .onSizeChanged { width = it.width.toFloat() }
            .pointerInput(enabled, layoutDirection) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        val logicalDragAmount = if (layoutDirection == LayoutDirection.Rtl) {
                            -dragAmount
                        } else {
                            dragAmount
                        }

                        val newOffset = (offset.value + logicalDragAmount).coerceIn(-width, width)

                        scope.launch {
                            offset.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        val progress = if (width > 0) offset.value / width else 0f

                        if (progress < -0.4f) {
                            onDelete()
                        } else if (progress > 0.4f) {
                            onEdit()
                        }

                        scope.launch {
                            offset.animateTo(0f)
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offset.animateTo(0f)
                        }
                    }
                )
            }
    ) {
        val progress = if (width > 0) offset.value / width else 0f

        // Background layer
        Box(modifier = Modifier.matchParentSize()) {
            backgroundContent(progress)
        }

        // Content layer
        Box(
            modifier = Modifier.offset {
                IntOffset(offset.value.roundToInt(), 0)
            }
        ) {
            content()
        }
    }
}