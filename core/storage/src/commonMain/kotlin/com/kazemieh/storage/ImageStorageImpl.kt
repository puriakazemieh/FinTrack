package com.kazemieh.storage

import com.kazemieh.common.ImageStorage


expect class ImageStorageImpl : ImageStorage {
    override suspend fun saveImage(bytes: ByteArray): String
    override suspend fun loadImage(path: String): ByteArray?
    override suspend fun deleteImage(path: String)
}
