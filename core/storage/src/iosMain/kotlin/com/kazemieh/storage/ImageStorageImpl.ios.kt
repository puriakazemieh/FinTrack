package com.kazemieh.storage

import com.kazemieh.common.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask

actual class ImageStorageImpl : ImageStorage {

    private val fileManager = NSFileManager.defaultManager

    actual override suspend fun saveImage(bytes: ByteArray): String = withContext(Dispatchers.Default) {
        val documentsDirectory = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first().toString()
        val fileName = "photo_${NSUUID().UUIDString}.jpg"
        val path = "$documentsDirectory/$fileName"
        path
    }

    actual override suspend fun loadImage(path: String): ByteArray? = withContext(Dispatchers.Default) {
        null
    }

    actual override suspend fun deleteImage(path: String) {
        withContext(Dispatchers.Default) {
        }
    }
}
