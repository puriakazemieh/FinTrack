package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.*

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
    isFullScreen: Boolean = true,
    topOffset: Dp = 40.dp,
    hero: @Composable (() -> Unit)? = null,
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    trailingContent: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val glassColors = LocalGlassColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .let { if (isFullScreen) it.fillMaxSize() else it.wrapContentHeight() }
                .background(Brush.verticalGradient(listOf(glassColors.bg1, glassColors.bg0)))
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
                    .padding(horizontal = 24.dp)
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
                        FintrackTitleMediumText(text = primaryButtonText, fontSize = 15.sp, fontWeight = FontWeight.W600)
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
