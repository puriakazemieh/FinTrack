package com.kazemieh.sms_reader

import android.app.NotificationManager
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
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("sms_id", -1)
        if (id == -1L) return

        when (intent.action) {
            "com.kazemieh.sms_reader.ACTION_IGNORE" -> {
                scope.launch {
                    smsDraftRepository.markSmsDraftAsUsed(id)
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(id.toInt())
                }
            }
        }
    }
}
