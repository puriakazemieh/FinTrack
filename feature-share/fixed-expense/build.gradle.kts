plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.fixed_expense"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "core:fixedExpenseKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    js {
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
                implementation(project(":feature-share:category"))
                implementation(project(":feature-share:source"))
                implementation(project(":core:designsystem"))
                implementation(project(":core:jalali"))

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation(libs.koin.compose)
                implementation(libs.koin.core)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.work.runtime.ktx)
            }
        }
    }
}
