package com.kazemieh.utilities.ui.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.sub_support
import fintrack.core.designsystem.generated.resources.support_chat
import fintrack.core.designsystem.generated.resources.support_chat_sub
import fintrack.core.designsystem.generated.resources.support_description
import fintrack.core.designsystem.generated.resources.support_email
import fintrack.core.designsystem.generated.resources.support_email_val
import fintrack.core.designsystem.generated.resources.support_phone
import fintrack.core.designsystem.generated.resources.support_phone_sub
import fintrack.core.designsystem.generated.resources.title_support
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupportScreen(
    onBackClick: () -> Unit
) {
    val space = LocalSpacing.current
    val uriHandler = LocalUriHandler.current
    val emailValue = stringResource(Res.string.support_email_val)

    FintrackScreen(
        title = stringResource(Res.string.title_support),
        sub = stringResource(Res.string.sub_support),
        onBack = onBackClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(space.medium),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            FintrackBodyMediumText(
                text = stringResource(Res.string.support_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SupportItem(
                icon = Icons.Default.HeadsetMic,
                title = stringResource(Res.string.support_phone),
                desc = stringResource(Res.string.support_phone_sub),
                onClick = { uriHandler.openUri(SupportLinks.ISSUES) }
            )

            SupportItem(
                icon = Icons.Default.Message,
                title = stringResource(Res.string.support_chat),
                desc = stringResource(Res.string.support_chat_sub),
                onClick = { uriHandler.openUri(SupportLinks.README) }
            )

            SupportItem(
                icon = Icons.Default.Email,
                title = stringResource(Res.string.support_email),
                desc = emailValue,
                onClick = { uriHandler.openUri("mailto:$emailValue") }
            )
        }
    }
}

private object SupportLinks {
    const val REPO = "https://github.com/puriakazemieh/FinTrack"
    const val ISSUES = "$REPO/issues/new"
    const val README = "$REPO/blob/develop/README.md"
}

@Composable
private fun SupportItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                FintrackTitleMediumText(text = title, fontWeight = FontWeight.Bold)
                FintrackBodyMediumText(
                    text = desc,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
