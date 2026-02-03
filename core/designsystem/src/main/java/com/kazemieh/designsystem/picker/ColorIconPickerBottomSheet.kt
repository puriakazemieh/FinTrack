package com.kazemieh.designsystem.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorIconPickerBottomSheet(
    initialColorId: Int? = null,
    initialIconId: Int? = null,
//    colors: List<PickableColor> = FinTrackPickerColors.colors,
    icons: List<PickableIcon> = FinTrackCategoryIcons.icons,
    onDismiss: () -> Unit,
    onSave: (color: PickableColor, icon: PickableIcon) -> Unit,
) {

    val colors = FinTrackPickerColors.rainbow()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val defaultColor = remember(colors, initialColorId) {
        colors.firstOrNull { it.id == initialColorId } ?: colors.first()
    }
    val defaultIcon = remember(icons, initialIconId) {
        icons.firstOrNull { it.id == initialIconId } ?: icons.first()
    }

    var selectedColor by remember(colors, initialColorId) { mutableStateOf(defaultColor) }
    var selectedIcon by remember(icons, initialIconId) { mutableStateOf(defaultIcon) }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(text = "انتخاب رنگ", style = MaterialTheme.typography.titleMedium)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(colors, key = { it.id }) { item ->
                    ColorChip(
                        item = item,
                        selected = item.id == selectedColor.id,
                        onClick = { selectedColor = item }
                    )
                }
            }

            Text(text = "انتخاب آیکون", style = MaterialTheme.typography.titleMedium)

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 320.dp)
            ) {
                items(icons, key = { it.id }) { icon ->
                    IconCell(
                        icon = icon,
                        tint = selectedColor.color,
                        selected = icon.id == selectedIcon.id,
                        onClick = { selectedIcon = icon }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss
                ) { Text("انصراف") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onSave(selectedColor, selectedIcon) }
                ) { Text("ذخیره") }
            }
        }
    }
}

@Composable
private fun ColorChip(
    item: PickableColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(item.color)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = bestOnColor(item.color),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun IconCell(
    icon: PickableIcon,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val border =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val bg =
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .border(1.dp, border, shape)
            .background(bg)
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon.resId),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

fun bestOnColor(bg: Color): Color =
    if (bg.luminance() > 0.55f) Color.Black else Color.White
