package com.kazemieh.tag.di

import com.kazemieh.tag.ui.TagViewModel
import com.kazemieh.tag.ui.add.AddTagViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionTagModule = module {
    viewModel {
        TagViewModel(
            getAllTag = get()
        )
    }
}
val transactionAddTagModule = module {
    viewModel {
        AddTagViewModel(
            addTagUseCase = get()
        )
    }
}