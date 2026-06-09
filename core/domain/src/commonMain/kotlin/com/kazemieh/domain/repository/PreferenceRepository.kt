package com.kazemieh.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun getLong(key: String, defaultValue: Long): Long
    fun putLong(key: String, value: Long)
    fun getStringFlow(key: String, defaultValue: String): Flow<String>
    fun remove(key: String)
    fun clear()
}
