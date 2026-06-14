package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import com.kazemieh.designsystem.component.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.GlassText3

/**
 * 2.8 ScreenHeader — back button + title/sub + action buttons
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    actions: List<HeaderAction> = emptyList(),
    center: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = if (center) Alignment.CenterVertically else Alignment.Top
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else if (onClose != null) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else if (!center) {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
        ) {
            FintrackHeadlineSmallText(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onBackground
            )
            sub?.let {
                FintrackLabelSmallText(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        trailingContent()

        if (actions.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { action ->
                    Box {
                        IconButton(onClick = action.onClick) {
                            Icon(
                                painter = action.icon,
                                contentDescription = action.label,
                                tint = action.color ?: MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (action.badge != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                        }
                    }
                }
            }
        } else if (onBack != null && !center) {
             Spacer(modifier = Modifier.width(48.dp)) // Mirror back button width
        }
    }
}

data class HeaderAction(
    val icon: Painter,
    val label: String,
    val onClick: () -> Unit,
    val color: Color? = null,
    val badge: Int? = null
)
