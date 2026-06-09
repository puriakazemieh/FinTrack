package com.kazemieh.preferences

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class SettingsObserver(private val settings: ObservableSettings) {

    fun getStringFlow(key: String, defaultValue: String): Flow<String> = callbackFlow {
        val listener = settings.addStringListener(key, defaultValue) {
            trySend(it)
        }
        awaitClose { listener.deactivate() }
    }.onStart { emit(settings.getString(key, defaultValue)) }

    fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> = callbackFlow {
        val listener = settings.addBooleanListener(key, defaultValue) {
            trySend(it)
        }
        awaitClose { listener.deactivate() }
    }.onStart { emit(settings.getBoolean(key, defaultValue)) }
}
