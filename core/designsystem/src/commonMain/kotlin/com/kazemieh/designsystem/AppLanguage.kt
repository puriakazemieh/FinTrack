package com.kazemieh.designsystem

/** Languages that FinTrack exposes independently from the device language. */
enum class AppLanguage(val languageTag: String) {
    PERSIAN("fa"),
    ENGLISH("en"),
    GERMAN("de");

    companion object {
        fun fromLanguageTag(value: String): AppLanguage =
            entries.firstOrNull { it.languageTag == value } ?: PERSIAN
    }
}
