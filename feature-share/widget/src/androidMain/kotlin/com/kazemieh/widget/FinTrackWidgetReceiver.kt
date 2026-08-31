package com.kazemieh.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class FinTrackWidgetReceiver : GlanceAppWidgetReceiver(), org.koin.core.component.KoinComponent {
    override val glanceAppWidget: GlanceAppWidget = FinTrackWidget()

    private val analytics: com.kazemieh.common.analytics.AnalyticsService by org.koin.core.component.inject()

    override fun onEnabled(context: android.content.Context) {
        super.onEnabled(context)
        analytics.track(com.kazemieh.common.analytics.ProductEvent.WidgetAdded)
    }

    override fun onDisabled(context: android.content.Context) {
        super.onDisabled(context)
        analytics.track(com.kazemieh.common.analytics.ProductEvent.WidgetRemoved)
    }
}
