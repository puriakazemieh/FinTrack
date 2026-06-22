package com.kazemieh.sms_reader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.AddFrame
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.amount_rial_label
import fintrack.core.designsystem.generated.resources.bank_label
import fintrack.core.designsystem.generated.resources.card_suffix_label
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.sms_sheet_sub
import fintrack.core.designsystem.generated.resources.sms_sheet_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetectionSheet(
    drafts: List<SmsDraft>,
    onDraftClick: (SmsDraft) -> Unit,
    onIgnore: (SmsDraft) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        AddFrame(
            title = stringResource(Res.string.sms_sheet_title),
            sub = stringResource(Res.string.sms_sheet_sub),
            primaryLabel = stringResource(Res.string.confirm),
            onPrimaryClick = onDismiss,
            onClose = onDismiss,
            showHero = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(drafts) { draft ->
                    SmsDraftItem(
                        draft = draft,
                        onClick = { onDraftClick(draft) },
                        onIgnore = { onIgnore(draft) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmsDraftItem(
    draft: SmsDraft,
    onClick: () -> Unit,
    onIgnore: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(glassColors.glass)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    tint = GlassBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.bank_label, draft.bankName),
                        fontWeight = FontWeight.Bold
                    )
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.amount_rial_label, draft.amount.toPersianPrice()),
                        color = GlassBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                FintrackLabelSmallText(
                    text = (draft.sourceIdentifier?.let { stringResource(Res.string.card_suffix_label, it) } ?: "") + draft.body.take(60) + "...",
                    maxLines = 1,
                    color = glassColors.text.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onIgnore) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = GlassRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
