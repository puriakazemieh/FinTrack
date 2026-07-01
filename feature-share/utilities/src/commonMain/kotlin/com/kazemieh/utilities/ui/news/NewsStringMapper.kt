package com.kazemieh.utilities.ui.news

import org.jetbrains.compose.resources.StringResource
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*

object NewsStringMapper {
    fun getTitle(id: String): StringResource = when(id) {
        "news_1" -> Res.string.news_title_1
        "news_2" -> Res.string.news_title_2
        "news_3" -> Res.string.news_title_3
        else -> Res.string.app_name
    }

    fun getSummary(id: String): StringResource = when(id) {
        "news_1" -> Res.string.news_summary_1
        "news_2" -> Res.string.news_summary_2
        "news_3" -> Res.string.news_summary_3
        else -> Res.string.app_name
    }

    fun getContent(id: String): StringResource = when(id) {
        "news_1" -> Res.string.news_content_1
        "news_2" -> Res.string.news_content_2
        "news_3" -> Res.string.news_content_3
        else -> Res.string.app_name
    }

    fun getCategory(cat: String): StringResource = when(cat) {
        "market" -> Res.string.news_cat_market
        "edu" -> Res.string.news_cat_edu
        else -> Res.string.app_name
    }

    fun getSource(src: String): StringResource = when(src) {
        "eco" -> Res.string.news_src_eco
        "app" -> Res.string.news_src_app
        else -> Res.string.app_name
    }
}
