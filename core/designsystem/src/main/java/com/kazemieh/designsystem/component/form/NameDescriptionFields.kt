package com.kazemieh.designsystem.component.form

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField


@Composable
fun NameDescriptionFields(
    name: String,
    onNameChange: (String) -> Unit,
    nameLabel: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionLabel: String,
    requiredStar: Boolean = true,
    between: @Composable () -> Unit = {}
) {
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
    between()
    FintrackOutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { FintrackBodyMediumText(text = descriptionLabel) }
    )
}