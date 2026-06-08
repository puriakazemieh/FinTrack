package com.kazemieh.designsystem.component.picker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker

interface ImagePicker {
    fun pickFromGallery()
    fun takePhoto()
}
