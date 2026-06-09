package com.kazemieh.data.repository

import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.preferences.FinTrackPreferences

class PreferenceRepositoryImpl(
    private val preferences: FinTrackPreferences
) : PreferenceRepository {
    override fun getString(key: String, defaultValue: String): String = preferences.getString(key, defaultValue)
    override fun putString(key: String, value: String) = preferences.putString(key, value)
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = preferences.getBoolean(key, defaultValue)
    override fun putBoolean(key: String, value: Boolean) = preferences.putBoolean(key, value)
    override fun getInt(key: String, defaultValue: Int): Int = preferences.getInt(key, defaultValue)
    override fun putInt(key: String, value: Int) = preferences.putInt(key, value)
    override fun getLong(key: String, defaultValue: Long): Long = preferences.getLong(key, defaultValue)
    override fun putLong(key: String, value: Long) = preferences.putLong(key, value)
    override fun remove(key: String) = preferences.remove(key)
    override fun clear() = preferences.clear()
}
