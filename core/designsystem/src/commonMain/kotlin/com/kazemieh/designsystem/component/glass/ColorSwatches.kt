package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.picker.bestOnColor

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun ColorSwatches(
    colors: List<Color>,
    pickedIndex: Int,
    onColorPick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Show ~3.5 items to hint scrolling
        val itemWidth = maxWidth / 3.5f
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            itemsIndexed(colors) { index, color ->
                val isSelected = index == pickedIndex
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(2.dp, GlassText, CircleShape)
                            else Modifier
                        )
                        .clickable { onColorPick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = bestOnColor(color),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
