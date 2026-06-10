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

    companion object {
        const val PREF_USER_NAME = "pref_user_name"
        const val PREF_USER_FAMILY = "pref_user_family"
        const val PREF_USER_EMAIL = "pref_user_email"
        const val PREF_USER_PHONE = "pref_user_phone"
        const val PREF_USER_BIRTHDAY = "pref_user_birthday"
        const val PREF_USER_CITY = "pref_user_city"
        const val PREF_USER_INCOME = "pref_user_income"
        const val PREF_USER_JOB = "pref_user_job"
        const val PREF_USER_GOAL = "pref_user_goal"
        const val PREF_USER_AVATAR = "pref_user_avatar"

        const val PREF_LOCK_ENABLED = "pref_lock_enabled"
        const val PREF_BIOMETRIC_ENABLED = "pref_biometric_enabled"
        const val PREF_HASHED_PIN = "pref_hashed_pin"

        const val PREF_NOTIF_BUDGET_ENABLED = "pref_notif_budget_enabled"
        const val PREF_NOTIF_INSTALLMENT_ENABLED = "pref_notif_installment_enabled"
        const val PREF_NOTIF_CHEQUE_ENABLED = "pref_notif_cheque_enabled"
        const val PREF_NOTIF_QUIET_HOURS_ENABLED = "pref_notif_quiet_hours_enabled"
        const val PREF_NOTIF_QUIET_START = "pref_notif_quiet_start"
        const val PREF_NOTIF_QUIET_END = "pref_notif_quiet_end"
    }
}
