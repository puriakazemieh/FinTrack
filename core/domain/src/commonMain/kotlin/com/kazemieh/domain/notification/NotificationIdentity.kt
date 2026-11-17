package com.kazemieh.domain.notification

/** Delivery-channel upgrades must not change a card's logical owner. */
fun canonicalNotificationChannel(channelId: String): String {
    val base = channelId.removeSuffix("_alerts_v1")
    return if (base == "installment_reminders") NotificationManager.CHANNEL_INSTALLMENT else base
}

/** Android identifies a card by (tag, integer ID); retain the full record ID in the tag. */
fun notificationTag(channelId: String, notificationId: Int, recordId: Long? = null): String =
    "fintrack:${canonicalNotificationChannel(channelId)}:${recordId?.takeIf { it > 0 } ?: notificationId.toLong()}"
