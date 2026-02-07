package com.kazemieh.designsystem.component.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.picker.FinTrackCategoryIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors


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
            FinTrackLeadingIcon(
                colorId = selectedColor.id,
                iconId = selectedIcon.resId,
                style = LeadingIconStyle.Badge,
                size = 44,
                iconSize = 22,
                modifier = Modifier.clickable(enabled = onIconClick != null) { onIconClick?.invoke() },
            )
            Spacer(Modifier.width(LocalSpacing.current.medium))
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