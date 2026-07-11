package com.kazemieh.designsystem

/**
 * User-selectable text size. [scale] multiplies the density fontScale so every `.sp`
 * dimension across the app grows or shrinks together.
 */
enum class TextScale(val scale: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f);

    fun next(): TextScale = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromName(name: String?): TextScale =
            entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}
