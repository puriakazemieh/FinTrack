package com.kazemieh.gamification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.Streak
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassAmberDeep
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun StreakHero(
    streak: Streak,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val spacing = LocalSpacing.current

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        tone = GlassTone.Strong
    ) {
        Column(
            modifier = Modifier
                .padding(spacing.large)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GlassAmber, GlassAmberDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            FintrackTitleLargeText(
                text = stringResource(Res.string.streak_days_label, streak.currentStreak.toString().toPersianDigits()),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            FintrackLabelMediumText(
                text = stringResource(Res.string.streak_desc),
                color = glassColors.text2
            )
        }
    }
}
