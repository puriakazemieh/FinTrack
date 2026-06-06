package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.model.Bank
import com.kazemieh.designsystem.picker.FinTrackIcons
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardItem(
    name: String,
    cardNumber: String,
    bank: Bank,
    iconId: Int?,
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current
    val selectedIcon = remember(iconId) {
        FinTrackIcons.findIcon(iconId)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(space.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space.medium)
            ) {
                // Bank Logo or Selected Icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(selectedIcon.resource),
                        contentDescription = null,
                        tint = if (selectedIcon.isTintable) Color.White else Color.Unspecified,
                        modifier = Modifier.size(35.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (bank != Bank.Unknown) {
                        FintrackTitleMediumText(
                            text = bank.name,
                            color = Color.White
                        )
                    }
                    FintrackBodySmallText(
                        text = name,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    FintrackTitleMediumText(
                        text = cardNumber.chunked(4).joinToString("-").toPersianDigits(),
                        color = Color.White
                    )
                }
            }
        }
    }
}
