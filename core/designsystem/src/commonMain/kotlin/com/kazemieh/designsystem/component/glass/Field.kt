package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText3

/**
 * 2.8 Field — glass form row
 */
@Composable
fun Field(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    hint: String? = null,
    error: Boolean = false,
    children: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        padding = 14.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (error) GlassRed else GlassText3,
                        fontSize = 12.sp
                    )
                    if (required) {
                        Text(
                            text = "*",
                            color = GlassRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
                
                Box {
                    children()
                }
            }
            
            hint?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassText3,
                    fontSize = 10.sp
                )
            }
        }
    }
}
