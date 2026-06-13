package com.kazemieh.asset.di

import com.kazemieh.asset.ui.AssetViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val assetModule = module {
    viewModelOf(::AssetViewModel)
}
