package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.model.Bank
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardItem(
    name: String,
    cardNumber: String,
    bank: Bank,
    colorId: Int?,
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current
    val colors = FinTrackPickerColors.rainbow()
    val pickedColor = colors.firstOrNull { it.id == colorId }?.color ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f), // standard card ratio
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            pickedColor,
                            pickedColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(space.large)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(bank.logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.height(32.dp)
                    )
                    FintrackTitleMediumText(
                        text = bank.name,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = cardNumber.chunked(4).joinToString("  "),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 2.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        FintrackBodySmallText(text = "صاحب کارت", color = Color.White.copy(alpha = 0.7f))
                        FintrackBodyMediumText(text = name, color = Color.White)
                    }
                    
                    Icon(
                        painter = painterResource(bank.icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
