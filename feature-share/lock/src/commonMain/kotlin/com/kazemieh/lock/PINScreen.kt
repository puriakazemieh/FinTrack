package com.kazemieh.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackHeadlineMediumText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.asString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.biometric_failed
import fintrack.core.designsystem.generated.resources.lock_confirm_pin
import fintrack.core.designsystem.generated.resources.lock_create_pin
import fintrack.core.designsystem.generated.resources.lock_enter_pin
import fintrack.core.designsystem.generated.resources.lock_error_mismatch
import fintrack.core.designsystem.generated.resources.lock_error_pin
import fintrack.core.designsystem.generated.resources.lock_verify_to_disable
import fintrack.core.designsystem.generated.resources.lock_welcome
import org.jetbrains.compose.resources.stringResource

@Composable
fun PINScreen(
    state: LockState,
    onIntent: (LockIntent) -> Unit
) {
    val space = LocalSpacing.current

    FintrackScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(space.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(space.extraLarge))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val title = when (state.mode) {
                    LockMode.UNLOCK -> stringResource(Res.string.lock_welcome)
                    LockMode.CREATE -> stringResource(Res.string.lock_create_pin)
                    LockMode.CONFIRM -> stringResource(Res.string.lock_confirm_pin)
                    LockMode.VERIFY_BEFORE_DISABLE -> stringResource(Res.string.lock_verify_to_disable)
                }

                FintrackHeadlineMediumText(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(space.medium))
                FintrackBodyLargeText(
                    text = state.subtitle?.asString() ?: stringResource(Res.string.lock_enter_pin),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(space.extraLarge))

                // PIN Dots
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(space.medium))
                    FintrackBodyLargeText(
                        text = when (state.error) {
                            "lock_error_pin" -> stringResource(Res.string.lock_error_pin)
                            "lock_error_mismatch" -> stringResource(Res.string.lock_error_mismatch)
                            "biometric_failed" -> stringResource(Res.string.biometric_failed)
                            else -> state.error
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Numeric Keypad
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                                    KeypadKey.BIOMETRIC -> state.mode == LockMode.UNLOCK && state.isBiometricEnabled
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
                                        KeypadKey.BIOMETRIC -> if (state.mode == LockMode.UNLOCK && state.isBiometricEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        KeypadKey.DELETE -> Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.3f
                                            )
                                        )

                                        else -> FintrackHeadlineSmallText(
                                            text = key.toString(),
                                            fontWeight = FontWeight.Medium,
                                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.3f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
