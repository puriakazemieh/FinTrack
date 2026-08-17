package com.kazemieh.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager as AndroidNotificationManagerSystem
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.payment_for
import fintrack.core.designsystem.generated.resources.notif_installment_title
import fintrack.core.designsystem.generated.resources.notif_installment_desc
import org.jetbrains.compose.resources.getString

class InstallmentActionReceiver : BroadcastReceiver(), KoinComponent {

    private val installmentUseCases: InstallmentUseCaseGroup by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val installmentId = intent.getLongExtra("installment_id", -1L)
        val notificationId = intent.getIntExtra("notification_id", -1)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManagerSystem

        when (intent.action) {
            "com.kazemieh.installment.ACTION_MARK_PAID" -> {
                if (installmentId == -1L) return
                scope.launch {
                    val installmentWithRelations = installmentUseCases.getInstallmentUseCase(installmentId)
                    val installment = installmentWithRelations?.installment ?: return@launch
                    
                    val paymentDesc = getString(Res.string.payment_for, installment.title)
                    val reminderTitle = getString(Res.string.notif_installment_title)
                    val reminderMsg = getString(Res.string.notif_installment_desc, installment.title)

                    installmentUseCases.markInstallmentAsPaidUseCase(
                        installmentId = installmentId,
                        transactionDescription = paymentDesc,
                        reminderTitle = reminderTitle,
                        reminderMessage = reminderMsg
                    )
                    nm.cancel(notificationId)
                }
            }
            "com.kazemieh.installment.ACTION_IGNORE" -> {
                nm.cancel(notificationId)
            }
        }
    }
}
