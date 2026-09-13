package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.LeadingIconStyle

/**
 * 2.8 AddFrame — full-screen add form
 */
@Composable
fun AddFrame(
    title: String,
    onClose: () -> Unit,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    sub: String? = null,
    iconId: Int? = null,
    colorId: Int? = null,
    heroName: String? = null,
    showHero: Boolean = true,
    backgroundBrush: Brush? = null,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    tertiaryLabel: String? = null,
    onTertiaryClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
    hero: @Composable (() -> Unit)? = null,
    preventSwipeDismiss: Boolean = true,
    horizontalPadding: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    // A form's residual drag must not dismiss its enclosing sheet before the form
    // reaches its top edge. The first top-edge pull is consumed and arms dismissal;
    // a later, separate pull is handed to ModalBottomSheet. This keeps normal
    // scrolling stable while preserving the familiar pull-to-dismiss gesture.
    var dismissArmed by remember { mutableStateOf(false) }
    var consumedTopEdgePullInGesture by remember { mutableStateOf(false) }
    val keepSheetOpenConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (consumed.y != 0f) {
                    dismissArmed = false
                }
                if (available.y <= 0f) return Offset.Zero

                return if (dismissArmed) {
                    Offset.Zero
                } else {
                    consumedTopEdgePullInGesture = true
                    available.copy(x = 0f)
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                // A release can still carry velocity from the first edge pull.
                // Never pass that velocity to the sheet: dismissal must require a
                // new drag, not the fling from the gesture that armed it.
                return available.copy(x = 0f)
            }
        }
    }
    val dismissGestureGate = Modifier
        .nestedScroll(keepSheetOpenConnection)
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                waitForUpOrCancellation()
                if (consumedTopEdgePullInGesture) {
                    dismissArmed = true
                    consumedTopEdgePullInGesture = false
                }
            }
        }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .then(
                if (preventSwipeDismiss) dismissGestureGate
                else Modifier
            )
            .then(
                if (backgroundBrush != null)
                    Modifier.background(backgroundBrush)
                else
                    Modifier
            )

    ) {
        if (backgroundBrush == null)
            FintrackBackgroundBlobs()
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = title,
                sub = sub,
                onClose = onClose,
                trailingContent = trailingContent,
                actions = if (onFilterClick != null) {
                    listOf(
                        HeaderAction(
                            icon = rememberVectorPainter(Icons.Default.FilterList),
                            label = "Filter",
                            onClick = onFilterClick
                        )
                    )
                } else emptyList()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = horizontalPadding)
            ) {
                // Hero Section
                if (showHero) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hero != null) {
                            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                                hero()
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                FinTrackLeadingIcon(
                                    colorId = colorId ?: 1,
                                    iconId = iconId ?: 1,
                                    style = LeadingIconStyle.Badge,
                                    size = 64.dp,
                                    iconSize = 32.dp,
                                )
                                if (!heroName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    FintrackHeadlineSmallText(
                                        text = heroName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }

                Spacer(Modifier.height(24.dp))
            }

            // Fixed CTA at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if ((tertiaryLabel != null) && (onTertiaryClick != null)) {
                    OutlinedButton(
                        onClick = onTertiaryClick,
                        modifier = Modifier
                            .weight(0.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        FintrackBodyLargeText(
                            text = tertiaryLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W700,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .weight(if (secondaryLabel != null || tertiaryLabel != null) 0.6f else 1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyLargeText(
                        text = primaryLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }

                if ((secondaryLabel != null) && (onSecondaryClick != null)) {
                    OutlinedButton(
                        onClick = onSecondaryClick,
                        modifier = Modifier
                            .weight(0.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        FintrackBodyLargeText(
                            text = secondaryLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }
        }
    }
}
