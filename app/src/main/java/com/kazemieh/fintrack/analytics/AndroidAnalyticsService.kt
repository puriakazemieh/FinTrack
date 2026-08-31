package com.kazemieh.fintrack.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.kazemieh.common.analytics.AnalyticsConsent
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.ProductEvent

class AndroidAnalyticsService : AnalyticsService {
    private val firebaseAnalytics = Firebase.analytics

    override fun track(event: ProductEvent) {
        val bundle = Bundle().apply {
            event.params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(event.eventName, bundle)
    }

    override fun setConsent(consent: AnalyticsConsent) {
        val consentMap = mapOf(
            FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to when (consent) {
                AnalyticsConsent.GRANTED -> FirebaseAnalytics.ConsentStatus.GRANTED
                AnalyticsConsent.DENIED -> FirebaseAnalytics.ConsentStatus.DENIED
                AnalyticsConsent.UNKNOWN -> FirebaseAnalytics.ConsentStatus.DENIED // safe default
            }
        )
        firebaseAnalytics.setConsent(consentMap)
    }

    override fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
}
