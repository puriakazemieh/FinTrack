package com.kazemieh.utilities.ui.news

import org.jetbrains.compose.resources.StringResource
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*

object NewsStringMapper {
    fun getTitle(id: String): StringResource = when(id) {
        "news_1" -> Res.string.news_title_1
        "news_2" -> Res.string.news_title_2
        "news_3" -> Res.string.news_title_3
        "edu_smart_goals" -> Res.string.edu_smart_goals_title
        "edu_financial_freedom_stages" -> Res.string.edu_freedom_stages_title
        "edu_financial_basket" -> Res.string.edu_basket_title
        else -> Res.string.app_name
    }

    fun getSummary(id: String): StringResource = when(id) {
        "news_1" -> Res.string.news_summary_1
        "news_2" -> Res.string.news_summary_2
        "news_3" -> Res.string.news_summary_3
        "edu_smart_goals" -> Res.string.edu_smart_goals_summary
        "edu_financial_freedom_stages" -> Res.string.edu_freedom_stages_summary
        "edu_financial_basket" -> Res.string.edu_basket_summary
        else -> Res.string.app_name
    }

    fun getContent(id: String): StringResource = when(id) {
        "news_1" -> Res.string.news_content_1
        "news_2" -> Res.string.news_content_2
        "news_3" -> Res.string.news_content_3
        "edu_smart_goals" -> Res.string.edu_smart_goals_content
        "edu_financial_freedom_stages" -> Res.string.edu_freedom_stages_content
        "edu_financial_basket" -> Res.string.edu_basket_content
        else -> Res.string.edu_smart_goals_title // Return a title instead of FinTrack to be visible
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
