package com.kazemieh.composeApp

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

//fun MainViewController() = ComposeUIViewController {
//    initKoin()
//    App()
//}

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }

    return ComposeUIViewController {
        App()
    }
}