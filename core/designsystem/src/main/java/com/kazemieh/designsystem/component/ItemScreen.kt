package com.kazemieh.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.asString

@Composable
fun ItemScreen(
    item: ItemUi,
    onItemClicked: (ItemUi) -> Unit,
    isDeleteShow: Boolean = false,
    isEditShow: Boolean = false,
    clickable: Boolean = true,
    onItemEditClicked: (ItemUi) -> Unit = {},
    onItemDeleteClicked: (ItemUi) -> Unit = {},
    content: @Composable (ItemUi) -> Unit = {}
) {
    val space = LocalSpacing.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = space.small)
            .clickable {
                if (clickable)
                    onItemClicked(item)
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = space.one)
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = space.medium,
                horizontal = space.large
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (item.iconId != null || item.colorId != null) {
                        FinTrackLeadingIcon(
                            colorId = item.colorId,
                            iconId = item.iconId,
                            style = LeadingIconStyle.Badge,
                            size = 40,
                            iconSize = 20
                        )
                        Spacer(Modifier.width(LocalSpacing.current.medium))
                    }
                    FintrackBodyMediumText(text = item.title.asString())
                    content(item)
                }

                if (isEditShow)
                    IconButton(onClick = { onItemEditClicked(item) }) {
                        Icon(
                            modifier = Modifier.weight(0.1f),
                            imageVector = Icons.Default.Edit,
                            contentDescription = Icons.Default.Edit.name,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                if (isDeleteShow)
                    IconButton(onClick = { onItemDeleteClicked(item) }) {
                        Icon(
                            modifier = Modifier.weight(0.1f),
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
            }
        }
    }
}