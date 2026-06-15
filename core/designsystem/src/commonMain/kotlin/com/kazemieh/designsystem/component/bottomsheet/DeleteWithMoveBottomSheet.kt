package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.ScreenHeader
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.delete_confirm_question
import fintrack.core.designsystem.generated.resources.move
import fintrack.core.designsystem.generated.resources.required_star
import fintrack.core.designsystem.generated.resources.transaction_delete
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteWithMoveBottomSheetContent(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    title: String? = null,
    itemName: String? = null,
    itemType: String? = null,
    deleteAllText: String,
    moveToAnotherText: String,
    targetPlaceholderText: String,
    targetValue: String?,
    isDeleteAll: Boolean,
    isTargetError: Boolean,
    onSelectDeleteAll: () -> Unit,
    onSelectMove: () -> Unit,
    onPickTarget: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String,
    dismissButtonText: String,
    isLoading: Boolean = false,
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current

    val finalTitle = title ?: if (itemName != null) {
        stringResource(Res.string.delete_confirm_question, itemType ?: "", itemName)
    } else {
        stringResource(Res.string.transaction_delete)
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(glassColors.bg1, glassColors.bg0)))
        ) {
            ScreenHeader(
                title = finalTitle,
                onClose = onDismiss
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(space.medium))

                GlassCard(
                    onClick = onSelectDeleteAll,
                    enabled = !isLoading,
                    padding = 8.dp,
                    tone = if (isDeleteAll) GlassTone.Strong else GlassTone.Default,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = isDeleteAll,
                            onClick = onSelectDeleteAll,
                            enabled = !isLoading
                        )
                        Spacer(Modifier.width(space.small))
                        FintrackBodyMediumText(
                            text = deleteAllText,
                            fontWeight = if (isDeleteAll) FontWeight.Bold else FontWeight.Medium,
                            color = glassColors.text
                        )
                    }
                }

                Spacer(Modifier.height(space.mediumSmall))

                GlassCard(
                    onClick = onSelectMove,
                    enabled = !isLoading,
                    padding = 8.dp,
                    tone = if (!isDeleteAll) GlassTone.Strong else GlassTone.Default,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = !isDeleteAll,
                            onClick = onSelectMove,
                            enabled = !isLoading
                        )
                        Spacer(Modifier.width(space.small))
                        FintrackBodyMediumText(
                            text = moveToAnotherText,
                            fontWeight = if (!isDeleteAll) FontWeight.Bold else FontWeight.Medium,
                            color = glassColors.text
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isDeleteAll,
                    modifier = Modifier.padding(top = space.medium)
                ) {
                    FintrackOutlinedTextField(
                        value = targetValue.orEmpty(),
                        onClick = onPickTarget,
                        readOnly = true,
                        enabled = false,
                        label = {
                            Row {
                                FintrackBodyMediumText(
                                    text = if (targetValue.isNullOrBlank()) targetPlaceholderText else stringResource(
                                        Res.string.move
                                    )
                                )
                                FintrackBodyMediumText(
                                    text = stringResource(Res.string.required_star),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        isError = isTargetError
                    )
                }
            }

            Spacer(modifier = Modifier.height(space.large))

            // Footer / CTAs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                GlassCard(
                    onClick = onConfirm,
                    enabled = !isLoading && (isDeleteAll || !targetValue.isNullOrEmpty()),
                    tone = GlassTone.Error,
                    padding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,

                        ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                        } else {
                            FintrackTitleMediumText(
                                text = confirmButtonText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }
                }

                GlassCard(
                    onClick = onDismiss,
                    enabled = !isLoading,
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
                            text = dismissButtonText,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
