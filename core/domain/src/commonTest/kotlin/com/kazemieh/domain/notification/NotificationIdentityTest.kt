package com.kazemieh.domain.notification

import kotlin.test.*

class NotificationIdentityTest {
    @Test fun everyOwningTypeHasAnIndependentIdentityEvenWithTheQuickAddInteger() {
        val channels = listOf(NotificationManager.CHANNEL_SMS, NotificationManager.CHANNEL_QUICK_ADD,
            NotificationManager.CHANNEL_BUDGET, NotificationManager.CHANNEL_INSTALLMENT, NotificationManager.CHANNEL_CHEQUE,
            NotificationManager.CHANNEL_DEBT, NotificationManager.CHANNEL_SHOPPING, NotificationManager.CHANNEL_NOTE,
            NotificationManager.CHANNEL_FIXED_EXPENSE)
        assertEquals(9, channels.map { notificationTag(it, 9001, 9001) }.toSet().size)
    }

    @Test fun fullRecordIdsRemainDistinctWhenIntegerIdsOverflow() {
        val first = 42L
        val second = 4_294_967_338L
        assertEquals(first.toInt(), second.toInt())
        assertNotEquals(notificationTag(NotificationManager.CHANNEL_SMS, first.toInt(), first),
            notificationTag(NotificationManager.CHANNEL_SMS, second.toInt(), second))
    }

    @Test fun channelUpgradesKeepTheLogicalOwnerAndFallbackIdsStayStable() {
        assertEquals(notificationTag(NotificationManager.CHANNEL_INSTALLMENT, -42, 10),
            notificationTag("installment_reminders_alerts_v1", -42, 10))
        assertEquals(notificationTag(NotificationManager.CHANNEL_SMS, -42),
            notificationTag("sms_detection_alerts_v1", -42, -1))
        assertNotEquals(notificationTag(NotificationManager.CHANNEL_SMS, -42),
            notificationTag(NotificationManager.CHANNEL_SMS, 42))
    }
}
