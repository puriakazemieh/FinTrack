// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatformPlugin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.4")
            force("com.fasterxml.jackson.core:jackson-databind:2.15.4")
            force("com.fasterxml.jackson.core:jackson-core:2.15.4")
            force("com.fasterxml.jackson.core:jackson-annotations:2.15.4")
        }
    }
}
