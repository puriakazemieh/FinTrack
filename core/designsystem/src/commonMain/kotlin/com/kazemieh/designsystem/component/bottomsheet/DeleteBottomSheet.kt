package com.kazemieh.designsystem.component.bottomsheet


import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackInlineFeedback
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.ScreenHeader
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.delete_confirm_question
import fintrack.core.designsystem.generated.resources.delete_named_item_question
import fintrack.core.designsystem.generated.resources.transaction_delete
import org.jetbrains.compose.resources.stringResource

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
    // Destructive confirmations are intentionally compact: they should not read like a screen.
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),

    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    isLoading: Boolean = false,
    message: String? = null,
    errorText: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val space = LocalSpacing.current

    val name = itemName?.trim()?.takeIf { it.isNotEmpty() }
    val type = itemType?.trim()?.takeIf { it.isNotEmpty() }
    val finalTitle = title ?: when {
        name != null && type != null -> stringResource(Res.string.delete_confirm_question, type, name)
        name != null -> stringResource(Res.string.delete_named_item_question, name)
        else -> stringResource(Res.string.transaction_delete)
    }

    ModalBottomSheet(
        onDismissRequest = { if (dismissEnabled && !isLoading) dismissClicked() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Decorative content must not determine the sheet's measured height.
            Box(Modifier.matchParentSize()) { FintrackBackgroundBlobs() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ScreenHeader(
                    title = finalTitle,
                    titleMaxLines = Int.MAX_VALUE,
                    onClose = if (dismissEnabled && !isLoading) dismissClicked else null
                )

                message?.let {
                    FintrackBodyMediumText(text = it, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                }
                content?.let { body ->
                    Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) { body() }
                }
                errorText?.let {
                    FintrackInlineFeedback(text = it, isError = true,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = space.mediumSmall))
                }

                // Footer / CTAs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp).height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(space.mediumLarge)
                ) {

                    Button(
                        onClick = confirmClicked,
                        enabled = confirmEnabled && !isLoading,
                        colors = confirmButtonColors,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,

                            ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = LocalContentColor.current,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                FintrackTitleMediumText(
                                    text = confirmButtonText,
                                    color = LocalContentColor.current,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { if (dismissEnabled && !isLoading) dismissClicked() },
                        enabled = dismissEnabled && !isLoading,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            FintrackTitleSmallText(
                                text = dismissButtonText,
                                fontWeight = FontWeight.W500,
                                color = LocalContentColor.current
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

