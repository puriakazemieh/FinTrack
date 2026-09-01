package com.kazemieh.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.component.inject
import org.koin.core.component.KoinComponent

class FinTrackWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget: GlanceAppWidget = FinTrackWidget()

    private val analytics: com.kazemieh.common.analytics.AnalyticsService by inject()

    override fun onEnabled(context: android.content.Context) {
        super.onEnabled(context)
        analytics.track(com.kazemieh.common.analytics.ProductEvent.WidgetAdded)
    }

    override fun onDisabled(context: android.content.Context) {
        super.onDisabled(context)
        analytics.track(com.kazemieh.common.analytics.ProductEvent.WidgetRemoved)
    }
}
