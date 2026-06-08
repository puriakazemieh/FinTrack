package com.kazemieh.preferences

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings

class FinTrackPreferences(private val settings: Settings) {
    @OptIn(ExperimentalSettingsApi::class)
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
