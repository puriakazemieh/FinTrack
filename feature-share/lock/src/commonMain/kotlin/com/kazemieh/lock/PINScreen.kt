package com.kazemieh.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackHeadlineMediumText
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun PINScreen(
    state: LockState,
    onIntent: (LockIntent) -> Unit
) {
    val space = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(space.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(space.extraLarge))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FintrackHeadlineMediumText(
                text = stringResource(Res.string.lock_welcome),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(space.medium))
            FintrackBodyLargeText(
                text = stringResource(Res.string.lock_enter_pin),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(space.extraLarge))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(space.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < state.pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(space.medium))
                FintrackBodyLargeText(
                    text = when(state.error) {
                        "lock_error_pin" -> stringResource(Res.string.lock_error_pin)
                        "biometric_failed" -> stringResource(Res.string.biometric_failed)
                        else -> state.error
                    },
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            val keypadRows = listOf(
                listOf(KeypadKey.DIGIT_1, KeypadKey.DIGIT_2, KeypadKey.DIGIT_3),
                listOf(KeypadKey.DIGIT_4, KeypadKey.DIGIT_5, KeypadKey.DIGIT_6),
                listOf(KeypadKey.DIGIT_7, KeypadKey.DIGIT_8, KeypadKey.DIGIT_9),
                listOf(KeypadKey.BIOMETRIC, KeypadKey.DIGIT_0, KeypadKey.DELETE)
            )

            keypadRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space.medium)
                ) {
                    row.forEach { key ->
                        val isEnabled = when (key) {
                            KeypadKey.BIOMETRIC -> true
                            KeypadKey.DELETE -> state.pin.isNotEmpty()
                            else -> state.pin.length < 4
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.5f)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(enabled = isEnabled) {
                                    if (key == KeypadKey.BIOMETRIC) {
                                        onIntent(LockIntent.AuthenticateBiometric)
                                    } else {
                                        onIntent(LockIntent.KeyPressed(key))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                KeypadKey.BIOMETRIC -> if (state.isBiometricEnabled) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                KeypadKey.DELETE -> Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                else -> Text(
                                    text = key.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
