package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassEdgeStrong
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.picker.rememberImagePicker
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_attachment_fa
import fintrack.core.designsystem.generated.resources.label_camera_fa
import fintrack.core.designsystem.generated.resources.label_gallery_fa
import fintrack.core.designsystem.generated.resources.label_optional_fa
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoDrop(
    photoBitmap: ImageBitmap?,
    onImagePicked: (ByteArray) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePicker = rememberImagePicker(onImagePicked = onImagePicked)

    GlassCard(
        modifier = modifier,
        padding = 14.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.label_attachment_fa),
                )
                FintrackLabelSmallText(
                    text = " (${stringResource(Res.string.label_optional_fa)})",
                    color = GlassText3,
                    fontSize = 8.sp
                )
            }
            if (photoBitmap != null) {
                ThumbnailCard(
                    bitmap = photoBitmap,
                    onRemove = onRemove,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    PhotoActionCard(
                        icon = Icons.Default.CameraAlt,
                        label = stringResource(Res.string.label_camera_fa),
                        onClick = { imagePicker.takePhoto() }
                    )
                    PhotoActionCard(
                        icon = Icons.Default.Image,
                        label = stringResource(Res.string.label_gallery_fa),
                        onClick = { imagePicker.pickFromGallery() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailCard(
    bitmap: ImageBitmap,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .rotate(-90f)
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColor)
            .border(1.5.dp, GlassEdgeStrong, RoundedCornerShape(12.dp))
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun PhotoActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColor)
            .border(1.5.dp, GlassEdgeStrong, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlassText3,
                modifier = Modifier.size(18.dp)
            )
            FintrackLabelSmallText(text = label, color = GlassText3)
        }
    }
}
