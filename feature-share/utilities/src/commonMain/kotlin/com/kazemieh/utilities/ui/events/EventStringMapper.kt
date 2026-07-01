package com.kazemieh.utilities.ui.events

import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource

object EventStringMapper {
    fun getTitle(id: String): StringResource = when(id) {
        "event_1" -> Res.string.event_title_1
        "event_2" -> Res.string.event_title_2
        "event_3" -> Res.string.event_title_3
        else -> Res.string.app_name
    }

    fun getDescription(id: String): StringResource = when(id) {
        "event_1" -> Res.string.event_desc_1
        "event_2" -> Res.string.event_desc_2
        "event_3" -> Res.string.event_desc_3
        else -> Res.string.app_name
    }
}
