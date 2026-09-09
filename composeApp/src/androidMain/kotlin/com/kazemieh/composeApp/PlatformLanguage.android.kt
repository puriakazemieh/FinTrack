package com.kazemieh.composeApp

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kazemieh.designsystem.AppLanguage

actual fun applyAppLanguage(language: AppLanguage) {
    if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != language.languageTag) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.languageTag)
        )
    }
}
