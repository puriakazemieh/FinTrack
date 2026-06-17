package com.kazemieh.designsystem

enum class ThemeMode {
    MANUAL,
    SYSTEM,
    SUNRISE_SUNSET,
    CUSTOM_TIME
}

enum class AppTheme {
    GLASS_DARK,
    GLASS_LIGHT,
    PLAIN_DARK,
    PLAIN_LIGHT;

    val isDark: Boolean
        get() = this == GLASS_DARK || this == PLAIN_DARK

    fun toggleDark(): AppTheme {
        return when (this) {
            GLASS_DARK -> GLASS_LIGHT
            GLASS_LIGHT -> GLASS_DARK
            PLAIN_DARK -> PLAIN_LIGHT
            PLAIN_LIGHT -> PLAIN_DARK
        }
    }

    fun toDark(): AppTheme {
        return when (this) {
            GLASS_DARK, GLASS_LIGHT -> GLASS_DARK
            PLAIN_DARK, PLAIN_LIGHT -> PLAIN_DARK
        }
    }

    fun toLight(): AppTheme {
        return when (this) {
            GLASS_DARK, GLASS_LIGHT -> GLASS_LIGHT
            PLAIN_DARK, PLAIN_LIGHT -> PLAIN_LIGHT
        }
    }
}
