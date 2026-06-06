package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import com.kazemieh.designsystem.component.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import org.jetbrains.compose.resources.stringResource

/**
 * 2.1 T — money / number display
 */
@Composable
fun MoneyText(
    amount: Long,
    modifier: Modifier = Modifier,
    suffix: String = stringResource(Res.string.currency_toman),
    size: Int = 13,
    weight: FontWeight = FontWeight.W500,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        FintrackTitleMediumText(
            text = amount.toPersianDigits(),
            fontSize = size.sp,
            fontWeight = weight,
            color = color
        )
        if (suffix.isNotEmpty()) {
            FintrackLabelSmallText(
                text = suffix,
                fontSize = (size * 0.55).sp,
                fontWeight = FontWeight.W400,
                color = color.copy(alpha = 0.42f),
                modifier = Modifier.padding(start = 5.dp, bottom = 2.dp)
            )
        }
    }
}
