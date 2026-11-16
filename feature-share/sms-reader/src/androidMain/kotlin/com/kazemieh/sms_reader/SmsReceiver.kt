package com.kazemieh.sms_reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.resolveSmsSource
import com.kazemieh.preferences.FinTrackPreferences
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.persiandatetime.extensions.toDateString
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.sms_detection_desc
import fintrack.core.designsystem.generated.resources.sms_detection_title
import fintrack.core.designsystem.generated.resources.sms_detection_amount
import fintrack.core.designsystem.generated.resources.currency_toman_full
import fintrack.core.designsystem.generated.resources.currency_rial_full
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsReceiver : BroadcastReceiver(), KoinComponent {

    private val smsDraftRepository: SmsDraftRepository by inject()
    private val transactionRepository: TransactionRepository by inject()
    private val notificationManager: NotificationManager by inject()
    private val preferenceUseCases: PreferenceUseCases by inject()
    private val analytics: com.kazemieh.common.analytics.AnalyticsService by inject()
    private val crashReporter: com.kazemieh.common.analytics.CrashReporter by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        // Respect the user's SMS-reading toggle: when disabled we don't parse messages or notify.
        // Defaults to enabled and fails OPEN — the OS RECEIVE_SMS permission is the real gate, so
        // any problem reading the preference must never silently swallow a real bank SMS.
        val smsReadingEnabled = try {
            preferenceUseCases.getBooleanPreference(
                FinTrackPreferences.PREF_SMS_READING_ENABLED,
                true
            )
        } catch (e: Exception) {
            crashReporter.recordException(e, "sms_preference_read_failed")
            true
        }
        if (!smsReadingEnabled) return

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val pendingResult = goAsync()
            scope.launch {
                try {
                    for ((sender, parts) in messages.groupBy { it.displayOriginatingAddress.orEmpty() }) {
                        val body = parts.joinToString("") { it.displayMessageBody.orEmpty() }
                        val receivedAt = parts.first().timestampMillis

                        val draft = BankParserRegistry.parse(sender, body)
                        if (draft == null) continue

                        val sources = transactionRepository.observeSources().first()
                        val detectedSource = resolveSmsSource(draft, sources)
                        
                        val finalDraft = draft.copy(
                            sourceId = detectedSource?.id,
                            timeStamp = receivedAt,
                            date = Instant.fromEpochMilliseconds(receivedAt)
                                .toPersianDateTime(TimeZone.currentSystemDefault()).toDateString()
                        )
                        val draftId = smsDraftRepository.addSmsDraft(finalDraft)
                        analytics.track(
                            com.kazemieh.common.analytics.ProductEvent.SmsDraftDetected(
                                sourceMatched = detectedSource != null
                            )
                        )

                        val title = getString(Res.string.sms_detection_title)
                        val bankTitle = getString(smsBank(finalDraft.bankName).title)
                        val displayCurrency = smsDisplayCurrency(preferenceUseCases.getStringPreference(
                            FinTrackPreferences.PREF_CURRENCY, ""))
                        val unitLabel = getString(if (displayCurrency.code == "IRT")
                            Res.string.currency_toman_full else Res.string.currency_rial_full)
                        val hideAmount = preferenceUseCases.getStringPreference(
                            FinTrackPreferences.PREF_HIDE_BALANCE, "false").toBoolean()
                        val messageStr = if (hideAmount) {
                            getString(Res.string.sms_detection_desc, bankTitle)
                        } else getString(Res.string.sms_detection_amount, bankTitle,
                            smsFormattedAmount(finalDraft.amount, displayCurrency, unitLabel))

                        // Use the persisted draft id as the notification id so both the deep link
                        // (pre-fill) and the "ignore" action target the correct draft row.
                        notificationManager.showStickyNotification(
                            id = draftId.toInt(),
                            title = title,
                            message = messageStr,
                            smsDraftId = draftId
                        )
                    }
                } catch (e: Exception) {
                    crashReporter.recordException(e, "sms_draft_processing_failed")
                    analytics.track(
                        com.kazemieh.common.analytics.ProductEvent.FeatureActionFailed(
                            "sms_draft_processing",
                            "processing_failed"
                        )
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
