plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlinxSerialization)
}
kotlin {
    androidLibrary {
        namespace = "com.kazemieh.data"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "core:commonKit"

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
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.datetime)
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:network"))
                implementation(project(":core:preferences"))
                implementation(project(":core:data-contract"))
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
        
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(project(":core:database"))
                implementation("app.cash.sqldelight:sqlite-driver:${libs.versions.sqldelight.get()}")
            }
        }
    }

}