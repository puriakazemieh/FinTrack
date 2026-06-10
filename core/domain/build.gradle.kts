plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.domain"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "core:domainKit"

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
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":core:common"))
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
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