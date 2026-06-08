package com.kazemieh.common.di

import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    includes(platformCommonModule)
}

expect val platformCommonModule: Module
