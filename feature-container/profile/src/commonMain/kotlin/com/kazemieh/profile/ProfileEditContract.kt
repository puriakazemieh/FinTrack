package com.kazemieh.profile

import com.kazemieh.designsystem.component.model.UiText

data class ProfileEditState(
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val birthday: String = "",
    val city: String = "",
    val income: String = "",
    val jobTitle: String = "",
    val financialGoal: String = "",
    val avatar: ByteArray? = null,
    val isLoading: Boolean = false,
    val completionPercentage: Float = 0f
)

sealed interface ProfileEditIntent {
    data class UpdateFirstName(val name: String) : ProfileEditIntent
    data class UpdateLastName(val family: String) : ProfileEditIntent
    data class UpdateNickname(val nickname: String) : ProfileEditIntent
    data class UpdateGender(val gender: String) : ProfileEditIntent
    data class UpdateEmail(val email: String) : ProfileEditIntent
    data class UpdatePhone(val phone: String) : ProfileEditIntent
    data class UpdateBirthday(val birthday: String) : ProfileEditIntent
    data class UpdateCity(val city: String) : ProfileEditIntent
    data class UpdateIncome(val income: String) : ProfileEditIntent
    data class UpdateJobTitle(val job: String) : ProfileEditIntent
    data class UpdateFinancialGoal(val goal: String) : ProfileEditIntent
    data class UpdateAvatar(val avatar: ByteArray?) : ProfileEditIntent
    data object RemoveAvatar : ProfileEditIntent
    data object SaveProfile : ProfileEditIntent
}

sealed interface ProfileEditEffect {
    data object ProfileSaved : ProfileEditEffect
    data class ShowError(val message: UiText) : ProfileEditEffect
}
