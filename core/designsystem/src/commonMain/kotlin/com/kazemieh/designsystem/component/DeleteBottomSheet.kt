package com.kazemieh.designsystem.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.designsystem.LocalSpacing
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.transaction_delete
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBottomSheet(
    title: String = stringResource(Res.string.transaction_delete),
    confirmButtonText: String = stringResource(Res.string.confirm),
    dismissButtonText: String = stringResource(Res.string.cancell_),
    confirmButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ),
    dismissButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    dismissClicked: () -> Unit,
    confirmClicked: () -> Unit,
    sheetState: SheetState,

    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val space = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = { if (dismissEnabled && !isLoading) dismissClicked() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column {
            FintrackTitleLargeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = space.large),
                text = title,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(space.large))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(space.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space.mediumSmall)
            ) {
                Button(
                    onClick = confirmClicked,
                    enabled = confirmEnabled && !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = confirmButtonColors
                ) {
                    FintrackTitleMediumText(
                        text = confirmButtonText,
                        color = Color.White
                    )
                }

                Button(
                    onClick = dismissClicked,
                    enabled = dismissEnabled && !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = dismissButtonColors
                ) {
                    FintrackTitleMediumText(
                        text = dismissButtonText,
                        color = Color.White
                    )
                }
            }
        }
    }
}

