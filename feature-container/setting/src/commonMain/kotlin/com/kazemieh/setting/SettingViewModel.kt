package com.kazemieh.setting

import androidx.lifecycle.ViewModel
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.ItemUi
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SettingViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingState())
    val state: StateFlow<SettingState> = _state

    fun onIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.OnClick -> _state.update { it.copy(selectedItem = intent.item) }
        }
    }


}

data class SettingState(
    val menuItems: List<ItemUi> = items,
    val selectedItem: ItemUi? = null,
)

val items: List<ItemUi> = listOf(
    ItemUi(id = ItemId.ITEM_1.id, title = UiText.StringResourceText(Res.string.category)),
    ItemUi(id = ItemId.ITEM_2.id, title = UiText.StringResourceText(Res.string.source)),
    ItemUi(id = ItemId.ITEM_3.id, title = UiText.StringResourceText(Res.string.tags)),
    ItemUi(id = ItemId.ITEM_4.id, title = UiText.StringResourceText(Res.string.persons)),
)

enum class ItemId(val id: Long) {
    ITEM_1(1),
    ITEM_2(2),
    ITEM_3(3),
    ITEM_4(4),
}


sealed interface SettingIntent {
    data class OnClick(val item: ItemUi? = null) : SettingIntent
}
