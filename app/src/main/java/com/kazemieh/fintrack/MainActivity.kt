package com.kazemieh.fintrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kazemieh.composeApp.App
import com.kazemieh.widget.WidgetUpdater

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle marketing attribution (UTM parameters)
        handleIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
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
