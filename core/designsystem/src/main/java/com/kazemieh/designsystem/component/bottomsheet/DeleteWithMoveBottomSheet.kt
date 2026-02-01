package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteWithMoveBottomSheetContent(
    sheetState: SheetState,
    title: String,
    deleteAllText: String,
    moveToAnotherText: String,
    targetTitleText: String,
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
) {
    val space = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column {
            FintrackTitleLargeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = space.large),
                text = title,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(space.large))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSelectDeleteAll() }
            ) {
                RadioButton(selected = isDeleteAll, onClick = onSelectDeleteAll)
                FintrackBodySmallText(text = deleteAllText)
            }

            Spacer(Modifier.height(space.mediumSmall))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSelectMove() }
            ) {
                RadioButton(selected = !isDeleteAll, onClick = onSelectMove)
                FintrackBodySmallText(text = moveToAnotherText)
            }

            AnimatedVisibility(
                visible = !isDeleteAll,
                modifier = Modifier.padding(horizontal = space.mediumSmall)
            ) {
                FintrackOutlinedTextField(
                    value = targetValue.orEmpty(),
                    onClick = onPickTarget,
                    readOnly = true,
                    enabled = false,
                    label = {
                        Row {
                            FintrackBodyMediumText(
                                text = if (targetValue.isNullOrBlank()) targetPlaceholderText else targetTitleText
                            )
                            FintrackBodyMediumText(
                                text = stringResource(com.kazemieh.designsystem.R.string.required_star),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = isTargetError
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(space.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space.mediumSmall)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    FintrackTitleMediumText(
                        text = confirmButtonText,
                        color = MaterialTheme.colorScheme.background
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackTitleMediumText(
                        text = dismissButtonText,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}
