package com.kazemieh.person.di

import com.kazemieh.person.ui.PersonViewModel
import com.kazemieh.person.ui.add.AddPersonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionPersonModule = module {
    viewModel {
        PersonViewModel(
            getAllPerson = get()
        )
    }
}
val transactionAddPersonModule = module {
    viewModel {
        AddPersonViewModel(
            addPersonUseCase = get()
        )
    }
}