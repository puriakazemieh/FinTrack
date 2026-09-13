import re

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'''    if \(isReady\) \{
        val isFirstRun = preferenceUseCases\.getBooleanPreference\(
            com\.kazemieh\.domain\.usecase\.SeedDataUseCase\.PREF_IS_FIRST_RUN,
            true
        \)
        val startDestination = if \(isFirstRun\) Screen\.Onboarding else Screen\.BottomBarGraph

        val baseDensity = androidx\.compose\.ui\.platform\.LocalDensity\.current
        val scaledDensity = androidx\.compose\.ui\.unit\.Density\(
            density = baseDensity\.density,
            fontScale = baseDensity\.fontScale \* TextScale\.fromName\(textScaleName\)\.scale
        \)

        CompositionLocalProvider\(
            LocalCurrency provides Currency\.valueOf\(currentCurrency\),
            LocalHideBalance provides hideBalance\.toBoolean\(\),
            LocalTextFont provides TextFont\.fromName\(textFontName\),
            LocalCalendarSystem provides CalendarSystem\.fromName\(calendarSystemName\),
            androidx\.compose\.ui\.platform\.LocalDensity provides scaledDensity
        \) \{
            FintrackTheme\(
                theme = calculatedTheme,
                accent = AccentPalette\.fromName\(accentName\)
            \) \{
                LockGate \{
                    FinTrackHost\(startDestination = startDestination\)
                \}
            \}
        \}
    \}'''

replacement = '''    val baseDensity = androidx.compose.ui.platform.LocalDensity.current
    val scaledDensity = androidx.compose.ui.unit.Density(
        density = baseDensity.density,
        fontScale = baseDensity.fontScale * TextScale.fromName(textScaleName).scale
    )

    CompositionLocalProvider(
        LocalCurrency provides Currency.valueOf(currentCurrency.ifEmpty { "IRT" }),
        LocalHideBalance provides hideBalance.toBoolean(),
        LocalTextFont provides TextFont.fromName(textFontName),
        LocalCalendarSystem provides CalendarSystem.fromName(calendarSystemName),
        androidx.compose.ui.platform.LocalDensity provides scaledDensity
    ) {
        FintrackTheme(
            theme = calculatedTheme,
            accent = AccentPalette.fromName(accentName)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            ) {
                if (isReady) {
                    val isFirstRun = preferenceUseCases.getBooleanPreference(
                        com.kazemieh.domain.usecase.SeedDataUseCase.PREF_IS_FIRST_RUN,
                        true
                    )
                    val startDestination = if (isFirstRun) Screen.Onboarding else Screen.BottomBarGraph

                    LockGate {
                        FinTrackHost(startDestination = startDestination)
                    }
                }
            }
        }
    }'''

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)
