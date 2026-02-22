package config

import org.gradle.api.JavaVersion


internal object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    const val TARGET_SDK = 36

    val JAVA_VERSION = JavaVersion.VERSION_21

    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
}

