package com.kazemieh.preferences

import com.russhwolf.multiplatform.settings.Settings
import com.russhwolf.multiplatform.settings.coroutines.toSuspendSettings
import com.russhwolf.multiplatform.settings.coroutines.SuspendSettings

class FinTrackPreferences(private val settings: Settings) {
    val suspendSettings: SuspendSettings = settings.toSuspendSettings()

    fun putString(key: String, value: String) = settings.putString(key, value)
    fun getString(key: String, defaultValue: String = ""): String = settings.getString(key, defaultValue)

    fun putInt(key: String, value: Int) = settings.putInt(key, value)
    fun getInt(key: String, defaultValue: Int = 0): Int = settings.getInt(key, defaultValue)

    fun putLong(key: String, value: Long) = settings.putLong(key, value)
    fun getLong(key: String, defaultValue: Long = 0L): Long = settings.getLong(key, defaultValue)

    fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = settings.getBoolean(key, defaultValue)

    fun remove(key: String) = settings.remove(key)
    fun clear() = settings.clear()
}
