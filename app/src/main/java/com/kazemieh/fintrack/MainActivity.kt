package com.kazemieh.fintrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color
import com.kazemieh.composeApp.App
import com.kazemieh.widget.WidgetUpdater
import com.kazemieh.notifications.AndroidNotificationManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        
        // Handle marketing attribution (UTM parameters)
        handleIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        com.kazemieh.composeApp.dispatchIncomingDeepLink(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        val notificationId = intent.getIntExtra(AndroidNotificationManager.EXTRA_NOTIFICATION_ID, -1)
        if (intent.hasExtra(AndroidNotificationManager.EXTRA_NOTIFICATION_ID)) {
            if (intent.getStringExtra(AndroidNotificationManager.EXTRA_NOTIFICATION_CHANNEL) ==
                com.kazemieh.domain.notification.NotificationManager.CHANNEL_QUICK_ADD
            ) {
                getSharedPreferences(AndroidNotificationManager.DELIVERY_PREFERENCES, MODE_PRIVATE)
                    .edit().putBoolean(AndroidNotificationManager.QUICK_ADD_DISMISSED, true).apply()
            }
            AndroidNotificationManager.cancelDeliveredNotification(this, notificationId,
                intent.getStringExtra(AndroidNotificationManager.EXTRA_NOTIFICATION_CHANNEL),
                intent.getStringExtra(AndroidNotificationManager.EXTRA_NOTIFICATION_TAG))
            intent.removeExtra(AndroidNotificationManager.EXTRA_NOTIFICATION_ID)
        }
        val intentData = intent?.data
        val utmSource = intentData?.getQueryParameter("utm_source")
        val utmCampaign = intentData?.getQueryParameter("utm_campaign")
        
        if (utmSource != null || utmCampaign != null) {
            try {
                val analytics = org.koin.java.KoinJavaComponent.getKoin().get<com.kazemieh.common.analytics.AnalyticsService>()
                analytics.track(com.kazemieh.common.analytics.ProductEvent.CampaignAttributed(
                    source = utmSource ?: "unknown",
                    campaign = utmCampaign ?: "unknown"
                ))
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdater.update(this)
    }
}
