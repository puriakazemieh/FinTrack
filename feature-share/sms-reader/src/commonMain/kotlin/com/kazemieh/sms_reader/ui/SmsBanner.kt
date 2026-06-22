package com.kazemieh.sms_reader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.sms_banner_desc
import fintrack.core.designsystem.generated.resources.sms_banner_text
import fintrack.core.designsystem.generated.resources.sms_sheet_sub
import org.jetbrains.compose.resources.stringResource

@Composable
fun SmsBanner(
    count: Int,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    WidgetCard(
        title = stringResource(Res.string.sms_banner_text, count.toPersianDigits()),
        accent = GlassBlue,
        onMore = onClick,
        more = stringResource(Res.string.sms_sheet_sub),
        onMenu = onClose,
        modifier = modifier
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.sms_banner_desc, count.toPersianDigits()),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
