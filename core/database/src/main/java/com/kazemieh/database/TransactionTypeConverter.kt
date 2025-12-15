package com.kazemieh.database

import androidx.room.TypeConverter
import com.kazemieh.common.model.TransactionType

class TransactionTypeConverter {

    @TypeConverter
    fun fromType(type: TransactionType): Int = type.count

    @TypeConverter
    fun toType(id: Int): TransactionType =
        TransactionType.fromInt(id)
}
