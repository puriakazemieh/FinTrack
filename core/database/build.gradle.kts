plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("FinTrackDatabase") {
            packageName.set("com.kazemieh.database")
//            schemaOutputDirectory.set(file("build/dbs"))
//            verifyMigrations.set(true)
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.database"
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
//        nodejs {
//            useSystemNodeJs = true
//        }
    }

    jvm()

    sourceSets {

        commonMain {
            dependencies {
                implementation(project(":core:common"))
                implementation(project(":core:data-contract"))
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
                implementation("app.cash.sqldelight:primitive-adapters:2.0.1")
                implementation(libs.koin.core)
                implementation(libs.kotlinx.serialization)
            }
        }
        androidMain {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.1")
            }
        }
        iosMain {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.1")
            }
        }

        jsMain {
            dependencies {
                implementation("app.cash.sqldelight:web-worker-driver:2.0.1")
            }
        }

        jvmMain {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }

    }
}
