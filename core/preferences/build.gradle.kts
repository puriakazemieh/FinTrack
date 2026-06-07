plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.preferences"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "core:preferencesKit"

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
                implementation(libs.russhwolf.settings)
                implementation(libs.russhwolf.settings.noarg)
                implementation(libs.russhwolf.settings.coroutines)
            }
        }
    }
}
