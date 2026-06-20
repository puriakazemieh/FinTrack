plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.backup_export"
        compileSdk = 35
        minSdk = 24
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:designsystem"))
                
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.poi)
                implementation(libs.poi.ooxml)
            }
        }
    }
}
