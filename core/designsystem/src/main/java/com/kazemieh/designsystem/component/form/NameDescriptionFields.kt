package com.kazemieh.designsystem.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.picker.FinTrackCategoryIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.picker.bestOnColor



@Composable
fun NameDescriptionFields(
    name: String,
    onNameChange: (String) -> Unit,
    nameLabel: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionLabel: String,
    requiredStar: Boolean = true,
    initialColorId: Int? = null,
    initialIconId: Int? = null,
    isIconShow: Boolean = true,
    onIconClick: (() -> Unit)? = null,
    between: @Composable () -> Unit = {}
) {
    val colors = FinTrackPickerColors.appTheme()

    val selectedColor = remember(colors, initialColorId) {
        colors.firstOrNull { it.id == initialColorId } ?: colors.first()
    }
    val selectedIcon = remember(FinTrackCategoryIcons.icons, initialIconId) {
        FinTrackCategoryIcons.icons.firstOrNull { it.id == initialIconId }
            ?: FinTrackCategoryIcons.icons.first()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isIconShow) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(selectedColor.color)
                    .clickable(enabled = onIconClick != null) { onIconClick?.invoke() }, // ✅ کلیک
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(selectedIcon.resId),
                    contentDescription = null,
                    tint = bestOnColor(selectedColor.color),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))
        }

        FintrackOutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {
                Row {
                    FintrackBodyMediumText(text = nameLabel)
                    if (requiredStar) {
                        FintrackBodyMediumText(
                            text = stringResource(com.kazemieh.designsystem.R.string.required_star),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }

    between()

    FintrackOutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { FintrackBodyMediumText(text = descriptionLabel) }
    )
}