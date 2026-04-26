package com.kazemieh.person.di

import com.kazemieh.person.ui.add.AddPersonViewModel
import com.kazemieh.person.ui.delete.DeletePersonViewModel
import com.kazemieh.person.ui.list.PersonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionPersonModule = module {
    viewModel {
        PersonViewModel(
            observePersonsUseCase = get()
        )
    }
}
val transactionDeletePersonModule = module {
    viewModel {
        DeletePersonViewModel(
            deletePersonUseCase = get()
        )
    }
}
val transactionAddPersonModule = module {
    viewModel {
        AddPersonViewModel(
            addPersonUseCase = get(),
            updatePersonUseCase = get()
        )
    }
}