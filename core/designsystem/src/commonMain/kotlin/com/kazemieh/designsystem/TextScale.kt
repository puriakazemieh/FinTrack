package com.kazemieh.designsystem

/**
 * User-selectable text size. [scale] multiplies the density fontScale so every `.sp`
 * dimension across the app grows or shrinks together.
 */
enum class TextScale(val scale: Float) {
    EXTRA_SMALL(0.75f),
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.30f);

    companion object {
        fun fromName(name: String?): TextScale =
            entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}

enum class TextFont {
    VAZIRMATN,
    SHABNAM,
    SAHEL;

    companion object {
        fun fromName(name: String?): TextFont =
            entries.firstOrNull { it.name == name } ?: VAZIRMATN
    }
}
