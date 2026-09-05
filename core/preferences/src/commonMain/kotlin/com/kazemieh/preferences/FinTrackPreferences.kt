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
        const val PREF_USER_NICKNAME = "pref_user_nickname"
        const val PREF_USER_GENDER = "pref_user_gender"
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
        const val PREF_SECURITY_QUESTION = "pref_security_question"
        const val PREF_SECURITY_ANSWER = "pref_security_answer"
        const val PREF_HIDE_BALANCE = "pref_hide_balance"
        const val PREF_DASHBOARD_WIDGETS = "pref_dashboard_widgets"
        const val PREF_BOTTOM_BAR_TABS = "pref_bottom_bar_tabs"
        const val PREF_DISABLED_TOOLS = "pref_disabled_tools"
        const val PREF_SMS_READING_ENABLED = "pref_sms_reading_enabled"
        const val PREF_TEXT_SCALE = "pref_text_scale"
        const val PREF_TEXT_FONT = "pref_text_font"

        const val PREF_THEME = "pref_theme"
        const val PREF_ACCENT = "pref_accent"
        const val PREF_THEME_MODE = "pref_theme_mode"
        const val PREF_FOLLOW_SYSTEM_THEME = "pref_follow_system_theme"
        const val PREF_THEME_START_TIME = "pref_theme_start_time"
        const val PREF_THEME_END_TIME = "pref_theme_end_time"
        const val PREF_CURRENCY = "pref_currency"
        const val PREF_CUSTOM_CURRENCIES = "pref_custom_currencies"
        const val PREF_BACKUP = "pref_backup"
        const val PREF_PUSH_NOTIF = "pref_push_notif"
        const val PREF_TX_ALERTS = "pref_tx_alerts"

        const val PREF_LAST_SYNC_TIME = "pref_last_sync_time"
        const val PREF_SYNC_ENABLED = "pref_sync_enabled"

        // Cloud AI advisor — the user brings their own OpenAI-compatible endpoint + key; nothing
        // is hardcoded, and the feature stays off until they configure it.
        const val PREF_AI_CLOUD_ENABLED = "pref_ai_cloud_enabled"
        const val PREF_AI_BASE_URL = "pref_ai_base_url"
        const val PREF_AI_API_KEY = "pref_ai_api_key"
        const val PREF_AI_MODEL = "pref_ai_model"

        const val PREF_NOTIF_BUDGET_ENABLED = "pref_notif_budget_enabled"
        const val PREF_NOTIF_INSTALLMENT_ENABLED = "pref_notif_installment_enabled"
        const val PREF_NOTIF_CHEQUE_ENABLED = "pref_notif_cheque_enabled"
        const val PREF_NOTIF_QUIET_HOURS_ENABLED = "pref_notif_quiet_hours_enabled"
        const val PREF_NOTIF_QUIET_START = "pref_notif_quiet_start"
        const val PREF_NOTIF_QUIET_END = "pref_notif_quiet_end"
        const val PREF_QUICK_ADD_NOTIF_ENABLED = "pref_quick_add_notif_enabled"

        const val PREF_ROUNDUP_ENABLED = "pref_roundup_enabled"
        const val PREF_ROUNDUP_GOAL_ID = "pref_roundup_goal_id"
        const val PREF_ROUNDUP_UNIT = "pref_roundup_unit"
    }
}
