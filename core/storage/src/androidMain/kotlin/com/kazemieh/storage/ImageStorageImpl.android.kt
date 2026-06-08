package com.kazemieh.storage

import android.content.Context
import com.kazemieh.common.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

actual class ImageStorageImpl(
    private val context: Context
) : ImageStorage {

    actual override suspend fun saveImage(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "photo_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        file.writeBytes(bytes)
        file.absolutePath
    }

    actual override suspend fun loadImage(path: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
    }

    actual override suspend fun deleteImage(path: String) {
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
