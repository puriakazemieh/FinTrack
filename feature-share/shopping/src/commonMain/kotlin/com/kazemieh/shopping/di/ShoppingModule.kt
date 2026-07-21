import com.kazemieh.shopping.ui.ShoppingViewModel
import com.kazemieh.domain.usecase.AddShoppingItemUseCase
import com.kazemieh.domain.usecase.DeleteShoppingItemUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedShoppingTagsUseCase
import com.kazemieh.domain.usecase.ObserveShoppingItemsUseCase
import com.kazemieh.domain.usecase.UpdateShoppingItemUseCase
import com.kazemieh.domain.usecase.UpdateShoppingPositionsUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shoppingModule = module {
    factory { ObserveShoppingItemsUseCase(get()) }
    factory { AddShoppingItemUseCase(get()) }
    factory { UpdateShoppingItemUseCase(get()) }
    factory { DeleteShoppingItemUseCase(get()) }
    factory { UpdateShoppingPositionsUseCase(get()) }
    factory { ObserveMostUsedShoppingTagsUseCase(get()) }

    viewModelOf(::ShoppingViewModel)
}
