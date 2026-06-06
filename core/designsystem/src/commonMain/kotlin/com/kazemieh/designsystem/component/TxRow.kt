package com.kazemieh.designsystem.component


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.separateWithCommas
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassBlueSoft
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassRedSoft
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.picker.FinTrackIcons
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_amount_with_unit
import fintrack.core.designsystem.generated.resources.unit_toman_short
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TxRow(item: TransactionWithRelations, onClick: () -> Unit) {
    val isIncome = item.transaction.type == TransactionType.INCOME
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    val color = when {
        isIncome -> GlassGreen
        isTransfer -> GlassBlue
        else -> GlassRed
    }
    val bgColor = when {
        isIncome -> GlassGreenSoft
        isTransfer -> GlassBlueSoft
        else -> GlassRedSoft
    }

    val icon = FinTrackIcons.findIcon(item.category.iconId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(GlassColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.small)
                .background(bgColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon.resource),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyMediumText(
                text = item.category.name,
                fontWeight = FontWeight.Bold,
                color = GlassText
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FintrackLabelSmallText(text = item.source.name, color = GlassText3)
                Box(modifier = Modifier.size(3.dp).background(GlassText3, CircleShape))
            }
        }

        Column(horizontalAlignment = Alignment.End) {

            val displayAmount =
                if (isTransfer) item.transaction.amountTransfer else item.transaction.amount

            FintrackTitleSmallText(
                text = stringResource(
                    Res.string.label_amount_with_unit,
                    displayAmount.toPersianPrice(),
                    stringResource(Res.string.unit_toman_short)
                ),
                fontWeight = FontWeight.W700,
                color = color
            )
            item.transaction.description?.takeIf { it.isNotEmpty() }?.let {
                FintrackLabelSmallText(text = it, color = GlassText3, maxLines = 1)
            }
        }
    }
}
