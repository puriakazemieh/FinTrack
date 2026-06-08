package com.kazemieh.common

interface ImageStorage {
    suspend fun saveImage(bytes: ByteArray): String
    suspend fun loadImage(path: String): ByteArray?
    suspend fun deleteImage(path: String)
}