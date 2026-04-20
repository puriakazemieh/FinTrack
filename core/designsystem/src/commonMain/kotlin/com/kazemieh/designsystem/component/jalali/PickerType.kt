package com.kazemieh.designsystem.component.jalali

sealed class PickerType {
    object Year : PickerType()
    object Month : PickerType()
    object Day : PickerType()
}