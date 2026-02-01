package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBottomSheetScaffold(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val space = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(space.mediumLarge)) {
            content()

            Spacer(Modifier.height(space.large))

            Button(
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                FintrackBodyMediumText(text = primaryButtonText)
            }
        }
    }
}
