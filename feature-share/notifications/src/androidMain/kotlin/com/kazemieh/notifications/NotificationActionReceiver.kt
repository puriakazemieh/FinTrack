package com.kazemieh.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.NotificationManager as AndroidNotificationManagerSystem
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.payment_for
import fintrack.core.designsystem.generated.resources.notif_installment_title
import fintrack.core.designsystem.generated.resources.notif_installment_desc
import org.jetbrains.compose.resources.getString

class NotificationActionReceiver : BroadcastReceiver(), KoinComponent {

    private val installmentUseCases: InstallmentUseCaseGroup by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", -1)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManagerSystem

        when (intent.action) {
            "com.kazemieh.notifications.ACTION_MANAGE" -> {
                val uri = intent.getStringExtra("uri")
                if (uri != null) {
                    val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        `package` = context.packageName
                    }
                    context.startActivity(launchIntent)
                }
                if (notificationId != -1) nm.cancel(notificationId)
            }
            "com.kazemieh.check.ACTION_IGNORE",
            "com.kazemieh.debt.ACTION_IGNORE",
            "com.kazemieh.installment.ACTION_IGNORE" -> {
                if (notificationId != -1) nm.cancel(notificationId)
            }
            "com.kazemieh.installment.ACTION_MARK_PAID" -> {
                val installmentId = intent.getLongExtra("installment_id", -1L)
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
                    if (notificationId != -1) nm.cancel(notificationId)
                }
            }
        }
    }
}
