package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.move
import fintrack.core.designsystem.generated.resources.required_star
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteWithMoveBottomSheetContent(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
    DeleteBottomSheet(
        title = title, itemName = itemName, itemType = itemType,
        sheetState = sheetState, confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText, dismissClicked = onDismiss,
        confirmClicked = onConfirm, confirmEnabled = isDeleteAll || !targetValue.isNullOrBlank(),
        isLoading = isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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

    }
}
