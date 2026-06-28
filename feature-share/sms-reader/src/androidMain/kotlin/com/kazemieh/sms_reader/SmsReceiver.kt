package com.kazemieh.sms_reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.notification.NotificationManager
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.sms_detection_desc
import fintrack.core.designsystem.generated.resources.sms_detection_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsReceiver : BroadcastReceiver(), KoinComponent {

    private val smsDraftRepository: SmsDraftRepository by inject()
    private val transactionRepository: TransactionRepository by inject()
    private val notificationManager: NotificationManager by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.displayOriginatingAddress ?: ""
                val body = message.displayMessageBody ?: ""

                val draft = BankParserRegistry.parse(sender, body)
                if (draft != null) {
                    scope.launch {
                        val finalDraft = draft.sourceIdentifier?.let { identifier ->
                            val source = transactionRepository.getSourceByIdentifier(identifier)
                            draft.copy(sourceId = source?.id)
                        } ?: draft
                        
                        smsDraftRepository.addSmsDraft(finalDraft)
                        
                        val title = getString(Res.string.sms_detection_title)
                        val messageStr = getString(Res.string.sms_detection_desc, finalDraft.bankName)
                        
                        notificationManager.showStickyNotification(
                            id = finalDraft.timeStamp.toInt(),
                            title = title,
                            message = messageStr
                        )
                    }
                }
            }
        }
    }
}
