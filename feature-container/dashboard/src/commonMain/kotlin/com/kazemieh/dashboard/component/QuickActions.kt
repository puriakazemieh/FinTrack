package com.kazemieh.dashboard.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuickActions(
    onActionClick: (TransactionType?) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionItem(
            label = stringResource(Res.string.outcoming),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = GlassRed,
            backgroundColor = GlassRedSoft,
            modifier = Modifier.weight(1f),
            onClick = { onActionClick(TransactionType.EXPENSE) }
        )
        QuickActionItem(
            label = stringResource(Res.string.incoming),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = GlassGreen,
            backgroundColor = GlassGreenSoft,
            modifier = Modifier.weight(1f),
            onClick = { onActionClick(TransactionType.INCOME) }
        )
        QuickActionItem(
            label = stringResource(Res.string.transfer),
            icon = Icons.Default.SwapHoriz,
            color = GlassBlue,
            backgroundColor = GlassBlueSoft,
            modifier = Modifier.weight(1f),
            onClick = { onActionClick(TransactionType.TRANSFER) }
        )
        QuickActionItem(
            label = stringResource(Res.string.search_placeholder).replace("...", ""),
            icon = Icons.Default.Search,
            color = GlassPurple,
            backgroundColor = GlassPurple.copy(alpha = 0.14f),
            modifier = Modifier.weight(1f),
            onClick = onSearchClick
        )
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        padding = 0.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            FintrackLabelSmallText(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.W500,
                color = GlassText
            )
        }
    }
}
