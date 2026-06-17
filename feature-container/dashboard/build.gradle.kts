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
        namespace = "com.kazemieh.dashboard"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "core:dashboardKit"

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
                implementation(project(":feature-share:transaction"))
                implementation(project(":feature-share:source"))
                implementation(project(":feature-share:budget"))
                implementation(project(":feature-share:installment"))
                implementation(project(":feature-share:asset"))

                implementation(project(":core:designsystem"))
                implementation(project(":core:domain"))
                implementation(project(":core:preferences"))

                implementation(project(":feature-share:check"))
                implementation(project(":feature-share:fixed-expense"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.datetime)

                implementation(libs.koin.compose)
                implementation(libs.koin.core)
                implementation(libs.koin.compose.viewmodel)
            }
        }


        androidMain {
            dependencies {
            }
        }

        iosMain {
            dependencies {
            }
        }

        jsMain {
            dependencies {
            }
        }

        jvmMain {
            dependencies {
            }
        }
    }

}