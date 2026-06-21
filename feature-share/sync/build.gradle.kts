plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.sync"
        compileSdk = 35
        minSdk = 24
    }
    val xcfName = "core:financialsourceKit"

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
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:network"))
                implementation(project(":core:designsystem"))
                
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.work.runtime.ktx)
            }
        }
    }
}
