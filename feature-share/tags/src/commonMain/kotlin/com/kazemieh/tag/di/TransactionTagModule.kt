package com.kazemieh.tag.di

import com.kazemieh.tag.ui.add.AddTagViewModel
import com.kazemieh.tag.ui.delete.DeleteTagViewModel
import com.kazemieh.tag.ui.list.TagViewModel
import com.kazemieh.tag.ui.detail.TagDetailViewModel
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
    viewModel { (tagId: Long) ->
        TagDetailViewModel(
            analytics = get(),
            tagId = tagId,
            observeTagsUseCase = get(),
            observeTransactionsUseCase = get(),
            noteRepository = get(),
            shoppingRepository = get(),
            debtRepository = get(),
            installmentRepository = get(),
            checkRepository = get(),
            fixedExpenseRepository = get(),
            budgetRepository = get()
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
