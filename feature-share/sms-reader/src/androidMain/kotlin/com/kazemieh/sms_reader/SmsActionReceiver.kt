package com.kazemieh.sms_reader

import com.kazemieh.domain.notification.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kazemieh.domain.repository.SmsDraftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsActionReceiver : BroadcastReceiver(), KoinComponent {

    private val smsDraftRepository: SmsDraftRepository by inject()
    private val analytics: com.kazemieh.common.analytics.AnalyticsService by inject()
    private val crashReporter: com.kazemieh.common.analytics.CrashReporter by inject()
    private val notificationManager: NotificationManager by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("sms_id", -1)
        if (id <= 0L || intent.action != "com.kazemieh.sms_reader.ACTION_IGNORE") return
        val pendingResult = goAsync()
        scope.launch {
            try {
                smsDraftRepository.ignoreSmsDraft(id)
                notificationManager.cancelNotification(intent.getIntExtra("notification_id", id.toInt()),
                    NotificationManager.CHANNEL_SMS, id)
                runCatching { analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationDismissed("ignore")) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "sms_ignore_failed")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
