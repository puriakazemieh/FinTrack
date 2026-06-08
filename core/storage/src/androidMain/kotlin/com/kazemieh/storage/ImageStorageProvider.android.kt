package com.kazemieh.storage

import com.kazemieh.common.ImageStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val storageModule: Module = module {
    single<ImageStorage> { ImageStorageImpl(get()) }
}
