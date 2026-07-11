package com.kazemieh.notifications

import android.Manifest
import com.kazemieh.domain.notification.NotificationManager
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManagerSystem
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notif_action_ignore
import fintrack.core.designsystem.generated.resources.notif_action_register
import fintrack.core.designsystem.generated.resources.notif_budget_exceeded_desc
import fintrack.core.designsystem.generated.resources.notif_budget_exceeded_title
import fintrack.core.designsystem.generated.resources.notif_channel_budget
import fintrack.core.designsystem.generated.resources.notif_channel_cheque
import fintrack.core.designsystem.generated.resources.notif_channel_desc
import fintrack.core.designsystem.generated.resources.notif_channel_installment
import fintrack.core.designsystem.generated.resources.notif_channel_note
import fintrack.core.designsystem.generated.resources.notif_channel_quick_add
import fintrack.core.designsystem.generated.resources.notif_channel_shopping
import fintrack.core.designsystem.generated.resources.notif_channel_sms
import android.net.Uri
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class AndroidNotificationManager(
    private val context: Context,
    private val preferenceUseCases: PreferenceUseCases
) : NotificationManager {

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    private val appIcon: Int
        get() = context.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info

    /**
     * A notification is only shown when its owning feature is on. Disabling a tool from
     * "Manage Tools" (or turning a channel off in notification settings, or turning SMS
     * reading off) silences the matching notifications everywhere.
     */
    private fun isChannelAllowed(channelId: String): Boolean {
        val disabledTools = ToolFeature.parseDisabled(
            preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_DISABLED_TOOLS, "")
        )
        fun toolEnabled(feature: ToolFeature) = feature !in disabledTools
        fun notifEnabled(key: String) = preferenceUseCases.getBooleanPreference(key, true)

        return when (channelId) {
            NotificationManager.CHANNEL_BUDGET ->
                toolEnabled(ToolFeature.BUDGETS) && notifEnabled(FinTrackPreferences.PREF_NOTIF_BUDGET_ENABLED)
            NotificationManager.CHANNEL_INSTALLMENT ->
                toolEnabled(ToolFeature.INSTALLMENT) && notifEnabled(FinTrackPreferences.PREF_NOTIF_INSTALLMENT_ENABLED)
            NotificationManager.CHANNEL_CHEQUE ->
                toolEnabled(ToolFeature.CHECK) && notifEnabled(FinTrackPreferences.PREF_NOTIF_CHEQUE_ENABLED)
            NotificationManager.CHANNEL_SHOPPING -> toolEnabled(ToolFeature.SHOPPING)
            NotificationManager.CHANNEL_NOTE -> toolEnabled(ToolFeature.NOTES)
            NotificationManager.CHANNEL_SMS ->
                preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_SMS_READING_ENABLED, true)
            else -> true
        }
    }

    override fun createChannels() {
        runBlocking {
            createChannel(
                NotificationManager.CHANNEL_BUDGET,
                getString(Res.string.notif_channel_budget)
            )
            createChannel(
                NotificationManager.CHANNEL_INSTALLMENT,
                getString(Res.string.notif_channel_installment)
            )
            createChannel(
                NotificationManager.CHANNEL_CHEQUE,
                getString(Res.string.notif_channel_cheque)
            )
            createChannel(
                NotificationManager.CHANNEL_SMS,
                getString(Res.string.notif_channel_sms)
            )
            createChannel(
                NotificationManager.CHANNEL_SHOPPING,
                getString(Res.string.notif_channel_shopping)
            )
            createChannel(
                NotificationManager.CHANNEL_NOTE,
                getString(Res.string.notif_channel_note)
            )
            createChannel(
                NotificationManager.CHANNEL_QUICK_ADD,
                getString(Res.string.notif_channel_quick_add),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_LOW
            )
        }
    }

    override fun createChannel(id: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val desc = runBlocking { getString(Res.string.notif_channel_desc) }
            val channel = NotificationChannel(id, name, importance).apply {
                description = desc
            }
            val androidManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManagerSystem
            androidManager.createNotificationChannel(channel)
        }
    }

    override fun showNotification(id: Int, title: String, message: String, channelId: String) {
        if (!hasPermission()) return
        if (!isChannelAllowed(channelId)) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManagerCompat.notify(id, notification)
    }

    override fun showStickyNotification(id: Int, title: String, message: String) {
        if (!hasPermission()) return
        if (!isChannelAllowed(NotificationManager.CHANNEL_SMS)) return

        // Deep-link straight into the add-transaction sheet so tapping the notification (or its
        // "register" action) actually opens the sheet instead of just landing on the dashboard.
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("fintrack://dashboard?showAddTransaction=true")
        ).apply {
            `package` = context.packageName
        }

        val pendingIntent = PendingIntent.getActivity(
            context, id, deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val registerLabel = runBlocking { getString(Res.string.notif_action_register) }
        val ignoreLabel = runBlocking { getString(Res.string.notif_action_ignore) }

        // Action for Register (same as main click)
        val registerAction = NotificationCompat.Action.Builder(
            0, registerLabel, pendingIntent
        ).build()

        // Action for Ignore (Broadcast)
        val ignoreIntent = Intent("com.kazemieh.sms_reader.ACTION_IGNORE").apply {
            putExtra("sms_id", id.toLong())
            `package` = context.packageName
        }
        val ignorePendingIntent = PendingIntent.getBroadcast(
            context, id, ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ignoreAction = NotificationCompat.Action.Builder(
            0, ignoreLabel, ignorePendingIntent
        ).build()

        val notification = NotificationCompat.Builder(context, NotificationManager.CHANNEL_SMS)
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(registerAction)
            .addAction(ignoreAction)
            .setAutoCancel(true)
            .build()

        notificationManagerCompat.notify(id, notification)
    }

    override fun showBudgetAlert(categoryId: Int, categoryName: String, progressPercentage: Int) {
        runBlocking {
            val title = getString(Res.string.notif_budget_exceeded_title)
            val message = getString(Res.string.notif_budget_exceeded_desc, categoryName, progressPercentage)
            showNotification(categoryId, title, message, NotificationManager.CHANNEL_BUDGET)
        }
    }

    override fun showQuickAddNotification(title: String, message: String, actionLabel: String) {
        if (!hasPermission()) return

        val id = NotificationManager.ID_QUICK_ADD
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("fintrack://dashboard?showAddTransaction=true")
        ).apply {
            `package` = context.packageName
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val action = NotificationCompat.Action.Builder(0, actionLabel, pendingIntent).build()

        val notification = NotificationCompat.Builder(context, NotificationManager.CHANNEL_QUICK_ADD)
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(action)
            .build()

        notificationManagerCompat.notify(id, notification)
    }

    override fun cancelNotification(id: Int) {
        notificationManagerCompat.cancel(id)
    }

    override fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun openSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
        }
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
