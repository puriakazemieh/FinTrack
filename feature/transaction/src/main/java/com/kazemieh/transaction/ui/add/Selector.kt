package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Selector(
    label: String,
    item: String,
    onClicked: () -> Unit
) {
    Column {
        Text(text = label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClicked() }
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(text = item)
        }

    }
}
