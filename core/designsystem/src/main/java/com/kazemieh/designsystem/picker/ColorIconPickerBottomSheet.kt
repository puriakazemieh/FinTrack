package com.kazemieh.designsystem.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FilterButton
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorIconPickerBottomSheet(
    initialColorId: Int? = null,
    initialIconId: Int? = null,
    onDismiss: () -> Unit,
    onSave: (color: PickableColor, icon: PickableIcon) -> Unit,
) {

    val colors = FinTrackPickerColors.rainbow()
    val icons = FinTrackCategoryIcons.icons

    val initialColorIndex = remember(initialColorId, colors) {
        colors.indexOfFirst { it.id == initialColorId }
            .takeIf { it >= 0 }
            ?: (initialColorId?.takeIf { it in colors.indices } ?: 0)
    }

    val initialIconIndex = remember(initialIconId, icons) {
        icons.indexOfFirst { it.id == initialIconId }
            .takeIf { it >= 0 }
            ?: (initialIconId?.takeIf { it in icons.indices } ?: 0)
    }


    val columns = 6

    val colorStartIndex = remember(initialColorIndex) {
        (initialColorIndex - 3).coerceAtLeast(0)
    }
    val iconStartIndex = remember(initialIconIndex) {
        val row = initialIconIndex / columns
        val startRow = (row - 2).coerceAtLeast(0)
        startRow * columns
    }

    val colorListState = rememberLazyListState(
        initialFirstVisibleItemIndex = colorStartIndex
    )
    val iconGridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = iconStartIndex
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val defaultColor = remember(colors, initialColorId) {
        colors.firstOrNull { it.id == initialColorId } ?: colors.first()
    }
    val defaultIcon = remember(icons, initialIconId) {
        icons.firstOrNull { it.id == initialIconId } ?: icons.first()
    }

    var selectedColor by remember(colors, initialColorId) { mutableStateOf(defaultColor) }
    var selectedIcon by remember(icons, initialIconId) { mutableStateOf(defaultIcon) }

    LaunchedEffect(initialColorIndex) {
        awaitFrame()
        colorListState.animateScrollToItem(initialColorIndex)
    }

    LaunchedEffect(initialIconIndex) {
        awaitFrame()
        iconGridState.animateScrollToItem(initialIconIndex)
    }

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

            Text(
                text = stringResource(R.string.choose_color),
                style = MaterialTheme.typography.titleMedium
            )

            LazyRow(
                state = colorListState,
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

            Text(
                text = stringResource(R.string.choose_icon),
                style = MaterialTheme.typography.titleMedium
            )

            LazyVerticalGrid(
                state = iconGridState,
                columns = GridCells.Fixed(columns),
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
                FilterButton(text = stringResource(R.string.cancell_), onClick = onDismiss)
                FilterButton(
                    text = stringResource(R.string.save_),
                    onClick = { onSave(selectedColor, selectedIcon) })
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
