package com.kazemieh.notifications

import android.Manifest
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.notification.notificationTag
import com.kazemieh.domain.notification.canonicalNotificationChannel
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManagerSystem
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notif_action_ignore
import fintrack.core.designsystem.generated.resources.notif_action_manage
import fintrack.core.designsystem.generated.resources.notif_action_register
import fintrack.core.designsystem.generated.resources.notif_budget_exceeded_desc
import fintrack.core.designsystem.generated.resources.notif_budget_exceeded_title
import fintrack.core.designsystem.generated.resources.notif_channel_budget
import fintrack.core.designsystem.generated.resources.notif_channel_cheque
import fintrack.core.designsystem.generated.resources.notif_channel_debt
import fintrack.core.designsystem.generated.resources.notif_channel_desc
import fintrack.core.designsystem.generated.resources.notif_channel_installment
import fintrack.core.designsystem.generated.resources.notif_channel_note
import fintrack.core.designsystem.generated.resources.notif_channel_quick_add
import fintrack.core.designsystem.generated.resources.notif_channel_shopping
import fintrack.core.designsystem.generated.resources.notif_channel_sms
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.title_fixed_expense_management
import android.net.Uri
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class AndroidNotificationManager(
    private val context: Context,
    private val preferenceUseCases: PreferenceUseCases,
    private val analytics: com.kazemieh.common.analytics.AnalyticsService
) : NotificationManager {

    companion object {
        const val DELIVERY_PREFERENCES = "fintrack_notification_delivery"
        const val QUICK_ADD_DISMISSED = "quick_add_dismissed"
        const val EXTRA_NOTIFICATION_ID = "fintrack_notification_id"
        const val EXTRA_NOTIFICATION_CHANNEL = "fintrack_notification_channel"
        const val EXTRA_NOTIFICATION_TAG = "fintrack_notification_tag"

        /** New cards are exact; legacy untagged cards are removed only when their channel matches. */
        fun cancelDeliveredNotification(context: Context, id: Int, channelId: String?, tag: String?) {
            val manager = context.getSystemService(AndroidNotificationManagerSystem::class.java) ?: return
            if (tag != null) {
                manager.cancel(tag, id)
                return
            }
            if (channelId == null) return
            val expected = canonicalNotificationChannel(channelId)
            val legacy = manager.activeNotifications.any { card ->
                card.id == id && card.tag == null && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canonicalNotificationChannel(card.notification.channelId) == expected
                } else {
                    card.notification.extras.getString(EXTRA_NOTIFICATION_CHANNEL) == expected
                }
            }
            if (legacy) manager.cancel(null, id)
        }
    }

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    private fun postNotification(id: Int, tag: String, channelId: String, notification: android.app.Notification) {
        cancelDeliveredNotification(context, id, channelId, null)
        notificationManagerCompat.notify(tag, id, notification)
    }

    private val appIcon: Int
        get() = context.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info

    /**
     * A notification is only shown when its owning feature is on. Disabling a tool from
     * "Manage Tools" (or turning a channel off in notification settings, or turning SMS
     * reading off) silences the matching notifications everywhere.
     */
    private fun isChannelAllowed(channelId: String): Boolean = try {
        val disabledTools = ToolFeature.parseDisabled(
            preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_DISABLED_TOOLS, "")
        )
        fun toolEnabled(feature: ToolFeature) = feature !in disabledTools
        fun notifEnabled(key: String) = preferenceUseCases.getBooleanPreference(key, true)

        when (channelId) {
            NotificationManager.CHANNEL_BUDGET ->
                toolEnabled(ToolFeature.BUDGETS) && notifEnabled(FinTrackPreferences.PREF_NOTIF_BUDGET_ENABLED)
            NotificationManager.CHANNEL_INSTALLMENT ->
                toolEnabled(ToolFeature.INSTALLMENT) && notifEnabled(FinTrackPreferences.PREF_NOTIF_INSTALLMENT_ENABLED)
            NotificationManager.CHANNEL_FIXED_EXPENSE ->
                toolEnabled(ToolFeature.FIXED_EXPENSE) && notifEnabled(FinTrackPreferences.PREF_NOTIF_FIXED_EXPENSE_ENABLED)
            NotificationManager.CHANNEL_CHEQUE ->
                toolEnabled(ToolFeature.CHECK) && notifEnabled(FinTrackPreferences.PREF_NOTIF_CHEQUE_ENABLED)
            NotificationManager.CHANNEL_DEBT -> 
                toolEnabled(ToolFeature.DEBT) && notifEnabled(FinTrackPreferences.PREF_NOTIF_DEBT_ENABLED)
            NotificationManager.CHANNEL_SHOPPING -> 
                toolEnabled(ToolFeature.SHOPPING) && notifEnabled(FinTrackPreferences.PREF_NOTIF_SHOPPING_ENABLED)
            NotificationManager.CHANNEL_NOTE -> 
                toolEnabled(ToolFeature.NOTES) && notifEnabled(FinTrackPreferences.PREF_NOTIF_NOTES_ENABLED)
            NotificationManager.CHANNEL_SMS ->
                preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_SMS_READING_ENABLED, true) &&
                    notifEnabled(FinTrackPreferences.PREF_NOTIF_BANK_RECEIPT_ENABLED)
            NotificationManager.CHANNEL_QUICK_ADD ->
                notifEnabled(FinTrackPreferences.PREF_QUICK_ADD_NOTIF_ENABLED)
            else -> true
        }
    } catch (e: Exception) {
        // Fail open: a preference/DI hiccup must never silently swallow notifications.
        true
    }

    override fun createChannels() {
        runBlocking {
            createChannel(
                NotificationManager.CHANNEL_BUDGET,
                getString(Res.string.notif_channel_budget),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_INSTALLMENT,
                getString(Res.string.notif_channel_installment),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_CHEQUE,
                getString(Res.string.notif_channel_cheque),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_DEBT,
                getString(Res.string.notif_channel_debt),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_SMS,
                getString(Res.string.notif_channel_sms),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_SHOPPING,
                getString(Res.string.notif_channel_shopping),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_NOTE,
                getString(Res.string.notif_channel_note),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_QUICK_ADD,
                getString(Res.string.notif_channel_quick_add),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
            )
            createChannel(
                NotificationManager.CHANNEL_FIXED_EXPENSE,
                getString(Res.string.title_fixed_expense_management),
                importance = AndroidNotificationManagerSystem.IMPORTANCE_HIGH
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

    private fun postingChannelId(channelId: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return channelId
        val manager = context.getSystemService(AndroidNotificationManagerSystem::class.java)
        val previous = manager.getNotificationChannel(channelId) ?: return channelId
        if (previous.importance >= AndroidNotificationManagerSystem.IMPORTANCE_HIGH ||
            previous.hasUserSetImportance() || previous.hasUserSetSound() || previous.sound == null
        ) {
            return channelId
        }
        // Only migrate app-created defaults. A user's channel choices always take precedence.
        val alertId = "${channelId}_alerts_v1"
        if (manager.getNotificationChannel(alertId) == null) {
            val alert = NotificationChannel(alertId, previous.name, AndroidNotificationManagerSystem.IMPORTANCE_HIGH).apply {
                description = previous.description
                group = previous.group
                setSound(previous.sound, previous.audioAttributes)
                enableVibration(previous.shouldVibrate())
                vibrationPattern = previous.vibrationPattern
                enableLights(previous.shouldShowLights())
                lightColor = previous.lightColor
                lockscreenVisibility = previous.lockscreenVisibility
                setShowBadge(previous.canShowBadge())
            }
            manager.createNotificationChannel(alert)
        }
        return alertId
    }

    private fun getClickPendingIntent(id: Int, uri: String, channelId: String, recordId: Long? = null): PendingIntent {
        val tag = notificationTag(channelId, id, recordId)
        val intent = requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(uri).buildUpon()
                .appendQueryParameter("notificationId", id.toString())
                .appendQueryParameter("notificationChannel", channelId)
                .appendQueryParameter("notificationTag", tag)
                .build()
            putExtra(EXTRA_NOTIFICATION_ID, id)
            putExtra(EXTRA_NOTIFICATION_CHANNEL, channelId)
            putExtra(EXTRA_NOTIFICATION_TAG, tag)
            removeCategory(Intent.CATEGORY_LAUNCHER)
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getDismissPendingIntent(id: Int, channelId: String, recordId: Long? = null): PendingIntent {
        val tag = notificationTag(channelId, id, recordId)
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.kazemieh.notifications.ACTION_DISMISSED"
            data = Uri.parse("fintrack-notification://dismiss").buildUpon().appendPath(tag).build()
            putExtra(EXTRA_NOTIFICATION_TAG, tag)
            putExtra("notification_id", id)
            putExtra("channel_id", channelId)
            `package` = context.packageName
        }
        return PendingIntent.getBroadcast(
            context, id + 5000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun showNotification(id: Int, title: String, message: String, channelId: String, extraId: Long) {
        if (!hasPermission()) return
        if (!isChannelAllowed(channelId)) return

        val recordId = extraId.takeIf { it > 0 }
        val tag = notificationTag(channelId, id, recordId)
        val defaultUri = when (channelId) {
            NotificationManager.CHANNEL_INSTALLMENT -> "fintrack://installment"
            NotificationManager.CHANNEL_FIXED_EXPENSE -> recordId?.let { "fintrack://fixed-expense/detail?expenseId=$it" }
                ?: "fintrack://fixed-expense"
            NotificationManager.CHANNEL_BUDGET -> "fintrack://budget"
            NotificationManager.CHANNEL_SHOPPING -> "fintrack://shopping"
            NotificationManager.CHANNEL_NOTE -> recordId?.let { "fintrack://note?noteId=$it" } ?: "fintrack://notes"
            else -> "fintrack://dashboard"
        }
        val builder = NotificationCompat.Builder(context, postingChannelId(channelId))
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setDeleteIntent(getDismissPendingIntent(id, channelId, recordId))
            .setContentIntent(getClickPendingIntent(id, defaultUri, channelId, recordId))

        when (channelId) {
            NotificationManager.CHANNEL_CHEQUE -> {
                builder.setContentIntent(getClickPendingIntent(id, "fintrack://check", channelId, recordId))

                val managePendingIntent = getClickPendingIntent(id, "fintrack://check", channelId, recordId)
                builder.addAction(0, runBlocking { getString(Res.string.notif_action_manage) }, managePendingIntent)

                val ignoreIntent = Intent("com.kazemieh.check.ACTION_IGNORE").apply {
                    setClass(context, NotificationActionReceiver::class.java)
                    putExtra("notification_id", id)
                    putExtra(EXTRA_NOTIFICATION_TAG, tag)
                    putExtra(EXTRA_NOTIFICATION_CHANNEL, channelId)
                    data = Uri.parse("fintrack-notification://ignore").buildUpon().appendPath(tag).build()
                    `package` = context.packageName
                }
                val ignorePendingIntent = PendingIntent.getBroadcast(context, id + 1000, ignoreIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(0, runBlocking { getString(Res.string.notif_action_ignore) }, ignorePendingIntent)
            }
            NotificationManager.CHANNEL_DEBT -> {
                builder.setContentIntent(getClickPendingIntent(id, "fintrack://debt", channelId, recordId))

                val managePendingIntent = getClickPendingIntent(id, "fintrack://debt", channelId, recordId)
                builder.addAction(0, runBlocking { getString(Res.string.notif_action_manage) }, managePendingIntent)

                val ignoreIntent = Intent("com.kazemieh.debt.ACTION_IGNORE").apply {
                    setClass(context, NotificationActionReceiver::class.java)
                    putExtra("notification_id", id)
                    putExtra(EXTRA_NOTIFICATION_TAG, tag)
                    putExtra(EXTRA_NOTIFICATION_CHANNEL, channelId)
                    data = Uri.parse("fintrack-notification://ignore").buildUpon().appendPath(tag).build()
                    `package` = context.packageName
                }
                val ignorePendingIntent = PendingIntent.getBroadcast(context, id + 1000, ignoreIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(0, runBlocking { getString(Res.string.notif_action_ignore) }, ignorePendingIntent)
            }
        }
        analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationReceived(channelId))
        postNotification(id, tag, channelId, builder.build())
    }

    override fun showStickyNotification(id: Int, title: String, message: String, smsDraftId: Long) {
        if (!hasPermission()) return
        if (!isChannelAllowed(NotificationManager.CHANNEL_SMS)) return

        val tag = notificationTag(NotificationManager.CHANNEL_SMS, id, smsDraftId)

        // Deep-link straight into the add-transaction sheet, carrying the SMS draft id so the sheet
        // opens pre-filled with the detected amount / type / source instead of a blank form.
        val deepLinkUri = "fintrack://dashboard?showAddTransaction=true&smsDraftId=$smsDraftId"

        val editLabel = runBlocking { getString(Res.string.edit) }
        val ignoreLabel = runBlocking { getString(Res.string.notif_action_ignore) }

        val managePendingIntent = getClickPendingIntent(id, deepLinkUri, NotificationManager.CHANNEL_SMS, smsDraftId)
        val editAction = NotificationCompat.Action.Builder(0, editLabel, managePendingIntent).build()

        // Action for Ignore (Broadcast)
        val ignoreIntent = Intent("com.kazemieh.sms_reader.ACTION_IGNORE").apply {
            setClassName(context, "com.kazemieh.sms_reader.SmsActionReceiver")
            putExtra("sms_id", smsDraftId)
            putExtra("notification_id", id)
            data = Uri.parse("fintrack-notification://sms-ignore").buildUpon().appendPath(tag).build()
            `package` = context.packageName
        }
        val ignorePendingIntent = PendingIntent.getBroadcast(
            context, id, ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ignoreAction = NotificationCompat.Action.Builder(
            0, ignoreLabel, ignorePendingIntent
        ).build()

        val notification = NotificationCompat.Builder(context, postingChannelId(NotificationManager.CHANNEL_SMS))
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(managePendingIntent)
            .setDeleteIntent(getDismissPendingIntent(id, NotificationManager.CHANNEL_SMS, smsDraftId))
            .addAction(editAction)
            .addAction(ignoreAction)
            .setAutoCancel(true)
            .build()
        analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationReceived(NotificationManager.CHANNEL_SMS))
        postNotification(id, tag, NotificationManager.CHANNEL_SMS, notification)
    }

    override fun showBudgetAlert(categoryId: Int, categoryName: String, progressPercentage: Int) {
        runBlocking {
            val title = getString(Res.string.notif_budget_exceeded_title)
            val message = getString(Res.string.notif_budget_exceeded_desc, categoryName, progressPercentage)
            showNotification(categoryId, title, message, NotificationManager.CHANNEL_BUDGET)
        }
    }

    override fun showInstallmentNotification(id: Int, title: String, message: String, installmentId: Long) {
        if (!hasPermission()) return
        if (!isChannelAllowed(NotificationManager.CHANNEL_INSTALLMENT)) return

        val tag = notificationTag(NotificationManager.CHANNEL_INSTALLMENT, id, installmentId)
        val deepLinkUri = "fintrack://installment"
        val pendingIntent = getClickPendingIntent(id, deepLinkUri, NotificationManager.CHANNEL_INSTALLMENT, installmentId)

        val registerLabel = runBlocking { getString(Res.string.notif_action_register) }
        val ignoreLabel = runBlocking { getString(Res.string.notif_action_ignore) }

        // Action for Mark as Paid (Broadcast)
        val registerIntent = Intent("com.kazemieh.installment.ACTION_MARK_PAID").apply {
            setClass(context, NotificationActionReceiver::class.java)
            putExtra("installment_id", installmentId)
            putExtra("notification_id", id)
            putExtra(EXTRA_NOTIFICATION_TAG, tag)
            putExtra(EXTRA_NOTIFICATION_CHANNEL, NotificationManager.CHANNEL_INSTALLMENT)
            data = Uri.parse("fintrack-notification://installment-action").buildUpon().appendPath(tag).build()
            `package` = context.packageName
        }
        val registerPendingIntent = PendingIntent.getBroadcast(
            context, id + 2000, registerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val registerAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save, registerLabel, registerPendingIntent
        ).build()

        // Action for Ignore (Broadcast)
        val ignoreIntent = Intent("com.kazemieh.installment.ACTION_IGNORE").apply {
            setClass(context, NotificationActionReceiver::class.java)
            putExtra("notification_id", id)
            putExtra(EXTRA_NOTIFICATION_TAG, tag)
            putExtra(EXTRA_NOTIFICATION_CHANNEL, NotificationManager.CHANNEL_INSTALLMENT)
            data = Uri.parse("fintrack-notification://installment-action").buildUpon().appendPath(tag).build()
            `package` = context.packageName
        }
        val ignorePendingIntent = PendingIntent.getBroadcast(
            context, id + 3000, ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ignoreAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel, ignoreLabel, ignorePendingIntent
        ).build()

        val notification = NotificationCompat.Builder(context, postingChannelId(NotificationManager.CHANNEL_INSTALLMENT))
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(getDismissPendingIntent(id, NotificationManager.CHANNEL_INSTALLMENT, installmentId))
            .addAction(registerAction)
            .addAction(ignoreAction)
            .setAutoCancel(true)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setOnlyAlertOnce(true)
            .build()
        analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationReceived(NotificationManager.CHANNEL_INSTALLMENT))
        postNotification(id, tag, NotificationManager.CHANNEL_INSTALLMENT, notification)
    }

    override fun showQuickAddNotification(title: String, message: String, actionLabel: String) {
        if (!hasPermission() || !isChannelAllowed(NotificationManager.CHANNEL_QUICK_ADD)) return
        val deliveryPreferences = context.getSharedPreferences(DELIVERY_PREFERENCES, Context.MODE_PRIVATE)
        if (deliveryPreferences.getBoolean(QUICK_ADD_DISMISSED, false)) return

        val id = NotificationManager.ID_QUICK_ADD
        val deepLinkUri = "fintrack://dashboard?showAddTransaction=true"
        val pendingIntent = getClickPendingIntent(id, deepLinkUri, NotificationManager.CHANNEL_QUICK_ADD)
        val action = NotificationCompat.Action.Builder(0, actionLabel, pendingIntent).build()

        val notification = NotificationCompat.Builder(context, postingChannelId(NotificationManager.CHANNEL_QUICK_ADD))
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(getDismissPendingIntent(id, NotificationManager.CHANNEL_QUICK_ADD))
            .addAction(action)
            .build()
        analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationReceived(NotificationManager.CHANNEL_QUICK_ADD))
        postNotification(id, notificationTag(NotificationManager.CHANNEL_QUICK_ADD, id), NotificationManager.CHANNEL_QUICK_ADD, notification)
    }

    override fun cancelNotification(id: Int) {
        if (id == NotificationManager.ID_QUICK_ADD) {
            cancelNotification(id, NotificationManager.CHANNEL_QUICK_ADD)
            context.getSharedPreferences(DELIVERY_PREFERENCES, Context.MODE_PRIVATE)
                .edit().remove(QUICK_ADD_DISMISSED).apply()
        } else notificationManagerCompat.cancel(id)
    }

    override fun cancelNotification(id: Int, channelId: String, recordId: Long?) {
        cancelDeliveredNotification(context, id, channelId, notificationTag(channelId, id, recordId))
        cancelDeliveredNotification(context, id, channelId, null)
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
