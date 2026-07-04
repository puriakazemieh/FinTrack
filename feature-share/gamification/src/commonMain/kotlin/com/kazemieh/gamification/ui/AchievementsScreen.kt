package com.kazemieh.gamification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.AchievementType
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.FintrackScreen
import androidx.compose.material3.LinearProgressIndicator
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.Composable

@Composable
fun AchievementsScreen(
    viewModel: GamificationViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    FintrackScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.large)
        ) {
            Spacer(modifier = Modifier.height(spacing.medium))

            LevelHeader(streak = state.streak)

            Spacer(modifier = Modifier.height(spacing.medium))

            StreakHero(streak = state.streak)

            Spacer(modifier = Modifier.height(spacing.large))

            FintrackLabelMediumText(
                text = stringResource(Res.string.badges_title),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(state.achievements) { achievement ->
                    AchievementBadge(achievement)
                }
            }
        }
    }
}

@Composable
private fun LevelHeader(streak: com.kazemieh.common.model.Streak) {
    val spacing = LocalSpacing.current
    val colors = LocalGlassColors.current

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(spacing.mediumLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackLabelLargeText(
                    text = stringResource(Res.string.label_level_n, streak.level.toString().toPersianDigits()),
                    fontWeight = FontWeight.Bold
                )
                FintrackLabelSmallText(
                    text = "${streak.xp % 1000} / 1000 ${stringResource(Res.string.label_xp_unit)}".toPersianDigits(),
                    color = colors.text2
                )
            }
            Spacer(modifier = Modifier.height(spacing.small))
            LinearProgressIndicator(
                progress = { streak.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = colors.glassEdge
            )
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    val spacing = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    val (labelRes, icon, color) = getAchievementDisplay(achievement.type)

    GlassCard(
        modifier = Modifier.aspectRatio(0.8f),
        tone = if (achievement.isUnlocked) GlassTone.Strong else GlassTone.Default
    ) {
        Column(
            modifier = Modifier
                .padding(spacing.small)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (achievement.isUnlocked) color.copy(alpha = 0.2f) else glassColors.glass
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (achievement.isUnlocked) icon else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (achievement.isUnlocked) color else glassColors.text2
                )
            }

            Spacer(modifier = Modifier.height(spacing.small))

            FintrackLabelSmallText(
                text = stringResource(labelRes),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = if (achievement.isUnlocked) glassColors.text else glassColors.text2
            )

            if (!achievement.isUnlocked && achievement.target > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { achievement.progress.toFloat() / achievement.target },
                    modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).clip(MaterialTheme.shapes.extraSmall),
                    color = color.copy(alpha = 0.5f),
                    trackColor = glassColors.glassEdge
                )
            }
        }
    }
}

private fun getAchievementDisplay(type: AchievementType): Triple<StringResource, androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (type) {
        AchievementType.FIRST_TRANSACTION -> Triple(Res.string.achievement_first_transaction, Icons.Default.Check, GlassGreen)
        AchievementType.ONE_WEEK_STREAK -> Triple(Res.string.achievement_one_week_streak, Icons.Default.Flag, GlassGreen)
        AchievementType.FIRST_BUDGET -> Triple(Res.string.achievement_first_budget, Icons.Default.AccountBalanceWallet, GlassGreen)
        AchievementType.SAVER -> Triple(Res.string.achievement_saver, Icons.Default.AdsClick, GlassAmber)
        AchievementType.HUNDRED_TRANSACTIONS -> Triple(Res.string.achievement_hundred_transactions, Icons.Default.EmojiEvents, GlassAmber)
        AchievementType.NO_EXTRA_EXPENSE -> Triple(Res.string.achievement_no_extra_expense, Icons.Default.Shield, GlassBlue)
        AchievementType.SIX_MONTHS_ACTIVE -> Triple(Res.string.achievement_six_months_active, Icons.Default.Star, GlassAmber)
        AchievementType.ZERO_DEBT -> Triple(Res.string.achievement_zero_debt, Icons.Default.SentimentSatisfied, GlassGreen)
        AchievementType.BUDGET_MASTER -> Triple(Res.string.achievement_budget_master, Icons.Default.MilitaryTech, GlassAmber)
    }
}
