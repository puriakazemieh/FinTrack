package com.kazemieh.notifications

import android.Manifest
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
import fintrack.core.designsystem.generated.resources.notif_channel_sms
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class AndroidNotificationManager(private val context: Context) : NotificationManager {

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    override fun createChannels() {
        createChannel(NotificationManager.CHANNEL_BUDGET, "Budget Reminders")
        createChannel(NotificationManager.CHANNEL_INSTALLMENT, "Installment Reminders")
        createChannel(NotificationManager.CHANNEL_CHEQUE, "Cheque Reminders")
        createChannel(NotificationManager.CHANNEL_SMS, "SMS Detection")
    }

    override fun createChannel(id: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, importance).apply {
                description = "FinTrack Notifications"
            }
            val androidManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManagerSystem
            androidManager.createNotificationChannel(channel)
        }
    }

    override fun showNotification(id: Int, title: String, message: String, channelId: String) {
        if (!hasPermission()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManagerCompat.notify(id, notification)
    }

    override fun showStickyNotification(id: Int, title: String, message: String) {
        if (!hasPermission()) return

        // Intent to open the app (MainActivity)
        val activityIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("sms_id", id.toLong())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, id, activityIntent,
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
            .setSmallIcon(android.R.drawable.ic_input_add)
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
