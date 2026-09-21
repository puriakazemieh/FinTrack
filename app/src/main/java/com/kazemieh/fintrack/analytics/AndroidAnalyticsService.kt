package com.kazemieh.fintrack.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics


import com.kazemieh.common.analytics.AnalyticsConsent
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.ProductEvent

class AndroidAnalyticsService(private val context: android.content.Context) : AnalyticsService {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

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
        when (event) {
            is ProductEvent.FeatureActionFailed -> crashlytics.log(
                "feature_action_failed:${event.params["feature_key"]}:${event.params["safe_error_code"]}"
            )
            is ProductEvent.AssetLinkedTransactionFailed -> crashlytics.log(
                "asset_linked_transaction_failed:${event.params["operation"]}"
            )
            else -> Unit
        }
    }

    override fun setConsent(consent: AnalyticsConsent) {
        val isGranted = consent == AnalyticsConsent.GRANTED
        firebaseAnalytics.setAnalyticsCollectionEnabled(isGranted)
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
