package com.kazemieh.designsystem.component.bottomsheet


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.delete_confirm_question
import fintrack.core.designsystem.generated.resources.transaction_delete
import org.jetbrains.compose.resources.stringResource


import com.kazemieh.designsystem.component.glass.GlassTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBottomSheet(
    title: String? = null,
    itemName: String? = null,
    itemType: String? = null,
    confirmButtonText: String = stringResource(Res.string.confirm),
    dismissButtonText: String = stringResource(Res.string.cancell_),
    confirmButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ),
    dismissClicked: () -> Unit,
    confirmClicked: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),

    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val space = LocalSpacing.current

    val finalTitle = title ?: if (itemName != null) {
        stringResource(Res.string.delete_confirm_question, itemType ?: "", itemName)
    } else {
        stringResource(Res.string.transaction_delete)
    }

    ModalBottomSheet(
        onDismissRequest = { if (dismissEnabled && !isLoading) dismissClicked() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = finalTitle,
                onClose = dismissClicked
            )

            Spacer(modifier = Modifier.height(space.large))

            // Footer / CTAs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              
                GlassCard(
                    onClick = confirmClicked,
                    enabled = confirmEnabled && !isLoading,
                    tone = GlassTone.Error,
                    padding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                        ,
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
                    onClick = { if (dismissEnabled && !isLoading) dismissClicked() },
                    enabled = dismissEnabled && !isLoading,
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

