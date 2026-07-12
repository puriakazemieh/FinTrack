package com.kazemieh.common.model

/**
 * Every optional "tool" the user can individually enable/disable from Settings.
 * Disabling a feature hides it from the Tools screen, its Dashboard widget (if any),
 * and the matching dimension in the transaction filter sheet — everything stays
 * enabled by default.
 */
enum class ToolFeature {
    SOURCES,
    CATEGORIES,
    TAGS,
    PERSONS,
    BUDGETS,
    FIXED_EXPENSE,
    SHOPPING,
    NOTES,
    GOALS,
    INSTALLMENT,
    DEBT,
    CHECK,
    ASSETS,
    FX_RATES,
    CONVERTER,
    NEWS,
    AI_ADVISOR,
    ACHIEVEMENTS,
    EVENTS,
    FINANCIAL_CALENDAR,
    FAQ,
    SUPPORT;

    companion object {
        /** Persisted as a CSV of disabled feature names; everything absent from it is enabled. */
        fun parseDisabled(csv: String): Set<ToolFeature> {
            if (csv.isBlank()) return emptySet()
            return csv.split(",")
                .map { it.trim() }
                .mapNotNull { name -> entries.firstOrNull { it.name == name } }
                .toSet()
        }

        fun serializeDisabled(disabled: Set<ToolFeature>): String =
            disabled.joinToString(",") { it.name }
    }
}
