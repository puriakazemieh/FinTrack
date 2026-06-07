package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.LeadingIconStyle

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
    heroName: String? = null,
    showHero: Boolean = true,
    backgroundBrush: Brush? = null,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    hero: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val defaultBrush = Brush.verticalGradient(listOf(GlassBg1, GlassBg0))
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush ?: defaultBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = title,
                sub = sub,
                onClose = onClose,
                actions = if (onFilterClick != null) {
                    listOf(
                        HeaderAction(
                            icon = rememberVectorPainter(Icons.Default.FilterList),
                            label = "Filter",
                            onClick = onFilterClick
                        )
                    )
                } else emptyList()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                // Hero Section
                if (showHero) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hero != null) {
                            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                                hero()
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                FinTrackLeadingIcon(
                                    colorId = colorId ?: 1,
                                    iconId = iconId ?: 1,
                                    style = LeadingIconStyle.Badge,
                                    size = 64.dp,
                                    iconSize = 32.dp,
                                )
                                if (!heroName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    FintrackHeadlineSmallText(
                                        text = heroName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }

                Spacer(Modifier.height(24.dp))
            }

            // Fixed CTA at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .weight(if (secondaryLabel != null) 0.6f else 1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyLargeText(
                        text = primaryLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }

                if ((secondaryLabel != null) && (onSecondaryClick != null)) {
                    OutlinedButton(
                        onClick = onSecondaryClick,
                        modifier = Modifier
                            .weight(0.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        FintrackBodyLargeText(
                            text = secondaryLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }
        }
    }
}
