package com.kazemieh.common.model

import kotlinx.serialization.Serializable

/**
 * مدل داده‌ای برای آیتم‌های لیست خرید.
 */
@Serializable
data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val isChecked: Boolean = false,
    val priority: Int = 0, // 0: Normal, 1: High
    val estimatedPrice: Double = 0.0,
    val reminderTime: Long? = null,
    val categoryId: Long? = null,
    val position: Int = 0
)
