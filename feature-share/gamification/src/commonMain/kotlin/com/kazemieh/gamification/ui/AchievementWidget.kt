package com.kazemieh.gamification.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Achievement
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.GlassCard
import androidx.compose.ui.draw.clip
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AchievementWidget(
    streak: com.kazemieh.common.model.Streak,
    achievements: List<Achievement>,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val glassColors = LocalGlassColors.current
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onMore
    ) {
        Column(modifier = Modifier.padding(spacing.mediumLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FintrackTitleMediumText(
                        text = stringResource(Res.string.achievements_title),
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Level Badge
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = stringResource(Res.string.label_level_n, streak.level.toString().toPersianDigits()),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TextButton(onClick = onMore) {
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.view_all),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            FintrackLabelMediumText(
                text = stringResource(
                    Res.string.badges_label_summary,
                    unlockedCount.toString().toPersianDigits(),
                    totalCount.toString().toPersianDigits()
                ),
                color = glassColors.text2
            )

            Spacer(modifier = Modifier.height(spacing.mediumSmall))

            // Streak Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_94), // trophy icon
                    contentDescription = null,
                    tint = com.kazemieh.designsystem.GlassAmber,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(Res.string.streak_days_label, streak.currentStreak.toString().toPersianDigits()) + " " + stringResource(Res.string.streak_desc),
                    style = MaterialTheme.typography.labelMedium,
                    color = glassColors.text
                )
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            // Show top 3 badges or progress bar
            LinearProgressIndicator(
                progress = { if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = glassColors.glassEdge
            )
        }
    }
}
