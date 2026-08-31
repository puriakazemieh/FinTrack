package com.kazemieh.tag.di

import com.kazemieh.tag.ui.add.AddTagViewModel
import com.kazemieh.tag.ui.delete.DeleteTagViewModel
import com.kazemieh.tag.ui.list.TagViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionTagModule = module {
    viewModel {
        TagViewModel(
            analytics = get(),
            observeTagsUseCase = get(),
            updateTagPositionsUseCase = get()
        )
    }
}
val transactionDeleteTagModule = module {
    viewModel {
        DeleteTagViewModel(
            analytics = get(),
            deleteTagUseCase = get()
        )
    }
}
val transactionAddTagModule = module {
    viewModel {
        AddTagViewModel(
            analytics = get(),
            addTagUseCase = get(),
            updateTagUseCase = get()
        )
    }
}