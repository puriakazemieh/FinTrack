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
            generateAsync = true
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
                implementation(libs.sqldelight.runtime)
                implementation("app.cash.sqldelight:async-extensions:${libs.versions.sqldelight.get()}")
                implementation("app.cash.sqldelight:coroutines-extensions:${libs.versions.sqldelight.get()}")
                implementation("app.cash.sqldelight:primitive-adapters:${libs.versions.sqldelight.get()}")
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.serialization)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.sqldelight.android.driver)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }

        jsMain {
            dependencies {
                implementation("app.cash.sqldelight:web-worker-driver:${libs.versions.sqldelight.get()}")

                implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
                implementation(npm("sql.js", "1.8.0"))
            }
        }

        jvmMain {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:${libs.versions.sqldelight.get()}")
            }
        }

    }
}
