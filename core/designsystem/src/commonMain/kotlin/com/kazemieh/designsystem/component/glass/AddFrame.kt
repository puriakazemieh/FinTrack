package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.component.*

/**
 * 2.8 AddFrame — full-screen add form
 */
@Composable
fun AddFrame(
    title: String,
    onClose: () -> Unit,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    sub: String? = null,
    iconId: Int? = null,
    colorId: Int? = null,
    hero: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "", // Title is handled in the hero section for AddFrame
                onBack = onClose
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                // Hero Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (hero != null) {
                        Box(modifier = Modifier.padding(bottom = 16.dp)) {
                            hero()
                        }
                    } else if (iconId != null && colorId != null) {
                         FinTrackLeadingIcon(
                            colorId = colorId,
                            iconId = iconId,
                            style = LeadingIconStyle.Badge,
                            size = 64.dp,
                            iconSize = 32.dp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    FintrackHeadlineLargeText(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W800
                    )
                    sub?.let {
                        FintrackBodyMediumText(
                            text = it,
                            color = GlassText2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }
            }

            // Fixed CTA at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyLargeText(
                        text = primaryLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
