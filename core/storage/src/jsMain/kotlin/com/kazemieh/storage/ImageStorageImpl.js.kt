package com.kazemieh.storage

import com.kazemieh.common.ImageStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import web.idb.IDBDatabase
import web.idb.IDBTransactionMode
import web.window.window
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Date

actual class ImageStorageImpl : ImageStorage {
    private val dbName = "fintrack_db"
    private val storeName = "images"
    private val version = 1

    private suspend fun getDatabase(): IDBDatabase = suspendCancellableCoroutine { continuation ->
        val request = window.indexedDB.open(dbName, version)
        request.onupgradeneeded = {
            val db = request.result as IDBDatabase
            if (!db.objectStoreNames.contains(storeName)) {
                db.createObjectStore(storeName)
            }
        }
        request.onsuccess = {
            continuation.resume(request.result as IDBDatabase)
        }
        request.onerror = {
            continuation.resumeWithException(Exception("Failed to open IndexedDB"))
        }
    }

    actual override suspend fun saveImage(bytes: ByteArray): String {
        val id = "photo_${Date.now()}_${(0..1000).random()}.jpg"
        val db = getDatabase()
        return suspendCancellableCoroutine { continuation ->
            val transaction = db.transaction(storeName, IDBTransactionMode.readwrite)
            val store = transaction.objectStore(storeName)
            val request = store.put(bytes.toUint8Array().asDynamic(), id)
            request.onsuccess = { continuation.resume(id) }
            request.onerror = { continuation.resumeWithException(Exception("Failed to save image")) }
        }
    }

    actual override suspend fun loadImage(path: String): ByteArray? {
        val db = getDatabase()
        return suspendCancellableCoroutine { continuation ->
            val transaction = db.transaction(storeName, IDBTransactionMode.readonly)
            val store = transaction.objectStore(storeName)
            val request = store.get(path)
            request.onsuccess = {
                val result = request.result
                if (result != null && result is Uint8Array) {
                    continuation.resume(result.toByteArray())
                } else {
                    continuation.resume(null)
                }
            }
            request.onerror = {
                continuation.resume(null)
            }
        }
    }

    actual override suspend fun deleteImage(path: String) {
        val db = getDatabase()
        suspendCancellableCoroutine { continuation ->
            val transaction = db.transaction(storeName, IDBTransactionMode.readwrite)
            val store = transaction.objectStore(storeName)
            val request = store.delete(path)
            request.onsuccess = { continuation.resume(Unit) }
            request.onerror = { continuation.resume(Unit) }
        }
    }

    private fun ByteArray.toUint8Array(): Uint8Array {
        val uint8Array = Uint8Array(this.size)
        for (i in indices) {
            uint8Array.asDynamic()[i] = this[i]
        }
        return uint8Array
    }

    private fun Uint8Array.toByteArray(): ByteArray {
        val byteArray = ByteArray(this.length)
        val int8Array = Int8Array(this.buffer)
        for (i in 0 until this.length) {
            byteArray[i] = int8Array.asDynamic()[i] as Byte
        }
        return byteArray
    }
}
