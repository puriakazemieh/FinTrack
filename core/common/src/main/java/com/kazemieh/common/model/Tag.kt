package com.kazemieh.common.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Tag(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val colorId: Int,
    val iconId: Int
)