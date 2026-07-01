package com.kazemieh.utilities.ui.faq

import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource

object FAQStringMapper {
    fun getQuestion(id: String): StringResource = when(id) {
        "faq_1" -> Res.string.faq_q_1
        "faq_2" -> Res.string.faq_q_2
        "faq_3" -> Res.string.faq_q_3
        else -> Res.string.app_name
    }

    fun getAnswer(id: String): StringResource = when(id) {
        "faq_1" -> Res.string.faq_a_1
        "faq_2" -> Res.string.faq_a_2
        "faq_3" -> Res.string.faq_a_3
        else -> Res.string.app_name
    }
}
