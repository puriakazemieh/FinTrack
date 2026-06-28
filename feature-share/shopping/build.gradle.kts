plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.shopping"
        compileSdk = 36
        minSdk = 24
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser()
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.datetime)
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:jalali"))
                implementation(project(":core:designsystem"))
                implementation(project(":feature-share:category"))
                implementation(project(":feature-share:notifications"))

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.koin.compose)
                implementation(libs.koin.core)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.reorderable)
            }
        }
    }
}
