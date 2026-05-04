plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)

//    id("convention.kotlin.multiplatform.compose")
}
compose.resources {
    publicResClass = true
}
kotlin {
    androidLibrary {
        namespace = "com.kazemieh.designsystem"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "core:designsystemKit"

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
//                implementation(libs.jalali.datepicker.compose)
//                implementation(libs.jalalicalendar)
                implementation(project(":core:common"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.serialization)


                implementation(libs.kotlinx.datetime)
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