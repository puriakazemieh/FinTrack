package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText

/**
 * 2.8 SheetFrame — bottom sheet shell
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetFrame(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sub: String? = null,
    horizontalPadding: Dp = 24.dp,
    isFullScreen: Boolean = true,
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    trailingContent: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Box(
            modifier = modifier
                .let { if (isFullScreen) it.fillMaxSize() else it.wrapContentHeight() }
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = if (isFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
            ) {
                ScreenHeader(
                    title = title,
                    sub = sub,
                    onClose = onDismiss,
                    trailingContent = trailingContent
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .let { if (isFullScreen) it.weight(1f) else it.wrapContentHeight() }
                        .padding(horizontal = horizontalPadding)
                ) {
                    content()
                }

                // Footer / CTAs
                if (primaryButtonText != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onPrimaryClick?.invoke() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            FintrackTitleMediumText(
                                text = primaryButtonText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.W600
                            )
                        }

                        if (secondaryButtonText != null) {
                            GlassCard(
                                onClick = onSecondaryClick,
                                padding = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FintrackTitleSmallText(
                                        text = secondaryButtonText,
                                        fontWeight = FontWeight.W500,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
