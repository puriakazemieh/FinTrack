package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.model.Tag

@Composable
fun TagSelector(
    tags: List<Tag>,
    selectedTags: Set<Tag>,
    onTagClick: (Tag) -> Unit
) {
    Column {
        Text(text = "تگ‌ها")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isSelected = tag in selectedTags
                FilterChip(
                    selected = isSelected,
                    onClick = { onTagClick(tag) },
                    label = { Text(tag.name) }
                )
            }
        }
    }
}
