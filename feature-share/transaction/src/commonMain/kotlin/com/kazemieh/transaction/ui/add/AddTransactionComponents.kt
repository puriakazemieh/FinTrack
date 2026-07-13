package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.SectionContainer
import com.kazemieh.designsystem.component.glass.AddChip
import com.kazemieh.designsystem.component.glass.RemovableChip
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.type_expense
import fintrack.core.designsystem.generated.resources.type_income
import fintrack.core.designsystem.generated.resources.type_transfer
import fintrack.core.designsystem.generated.resources.sms_ref_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun SmsReferenceBanner(
    draft: SmsDraft,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GlassBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(GlassBlue.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = GlassBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.sms_ref_label, draft.bankName),
                        color = GlassBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FintrackLabelSmallText(
                text = draft.body,
                color = glassColors.text.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GlassSegmentedSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val types = listOf(
        TransactionType.EXPENSE to stringResource(Res.string.type_expense),
        TransactionType.INCOME to stringResource(Res.string.type_income),
        TransactionType.TRANSFER to stringResource(Res.string.type_transfer)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(glassColors.glass, RoundedCornerShape(14.dp))
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        types.forEach { (type, label) ->
            val active = type == selectedType
            val color = when (type) {
                TransactionType.INCOME -> GlassGreen
                TransactionType.TRANSFER -> GlassBlue
                else -> GlassRed
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (active) Modifier
                            .background(color.copy(alpha = 0.14f))
                            .border(1.dp, color.copy(alpha = 0.33f), RoundedCornerShape(10.dp))
                        else Modifier
                    )
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                FintrackBodyMediumText(
                    text = label,
                    color = if (active) color else glassColors.text3
                )
            }
        }
    }
}
