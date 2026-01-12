package com.kazemieh.setting

import androidx.lifecycle.ViewModel
import com.kazemieh.common.UiText
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.ItemUi
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SettingViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingState())
    val state: StateFlow<SettingState> = _state

    fun onIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.OnClick -> _state.update { it.copy(selectedItem = intent.item) }
            is SettingIntent.SetCategory -> _state.update { it.copy(category = intent.category,selectedItem=null) }
            is SettingIntent.SetPerson -> _state.update { it.copy(persons = intent.persons) }
            is SettingIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is SettingIntent.SetTags -> _state.update { it.copy(tags = intent.tags) }
//            SettingIntent.OnCategoryShow -> _state.update { it.copy(isCategoryShow = !_state.value.isCategoryShow) }
//            SettingIntent.OnPersonShow -> _state.update { it.copy(isPersonShow = !_state.value.isPersonShow) }
//            SettingIntent.OnSourceShow -> _state.update { it.copy(isSourceShow = !_state.value.isSourceShow) }
//            SettingIntent.OnTagsShow -> _state.update { it.copy(isTagShow = !_state.value.isTagShow) }
        }
    }


}

data class SettingState(
    val menuItems: List<ItemUi> = items,

    val selectedItem: ItemUi? = null,

    val category: Category? = null,
    val source: Source? = null,
    val tags: Tag? = null,
    val persons: Person? = null,

//    val isSourceShow: Boolean = false,
//    val isCategoryShow: Boolean = false,
//    val isTagShow: Boolean = false,
//    val isPersonShow: Boolean = false,
)

val items: List<ItemUi> = listOf(
    ItemUi(id = ItemId.ITEM_1.id, title = UiText.StringResource(R.string.category)),
    ItemUi(id = ItemId.ITEM_2.id, title = UiText.StringResource(R.string.source)),
    ItemUi(id = ItemId.ITEM_3.id, title = UiText.StringResource(R.string.tags)),
    ItemUi(id = ItemId.ITEM_4.id, title = UiText.StringResource(R.string.persons)),
)

enum class ItemId(val id: Long) {
    ITEM_1(1),
    ITEM_2(2),
    ITEM_3(3),
    ITEM_4(4),
}


sealed interface SettingIntent {
    data class OnClick(val item: ItemUi?=null) : SettingIntent
    data class SetCategory(val category: Category? = null) : SettingIntent
//    data object OnCategoryShow : SettingIntent
    data class SetSource(val source: Source? = null) : SettingIntent
//    data object OnSourceShow : SettingIntent
    data class SetTags(val tags: Tag? = null) : SettingIntent
//    data object OnTagsShow : SettingIntent
    data class SetPerson(val persons: Person? = null) : SettingIntent
//    data object OnPersonShow : SettingIntent
}
