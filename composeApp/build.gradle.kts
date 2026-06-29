import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "fintrack.composeapp.generated.resources"
}

kotlin {

    androidLibrary {
        namespace = "com.kazemieh.composeApp"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "composAppKit"

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


    jvm()

    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material.icons.extended)

            implementation(libs.navigation.compose)
            implementation(project(":core:common"))
            implementation(project(":core:storage"))
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:database"))
            implementation(project(":core:preferences"))
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(project(":core:money"))
            implementation(project(":feature-share:asset"))
            implementation(project(":feature-share:transaction"))
            implementation(project(":feature-share:category"))
            implementation(project(":feature-share:installment"))
            implementation(project(":feature-share:goals"))
            implementation(project(":feature-share:source"))
            implementation(project(":feature-share:tags"))
            implementation(project(":feature-container:transactions"))
            implementation(project(":feature-share:person"))
            implementation(project(":feature-share:ai-insights"))
            implementation(project(":feature-share:debt"))
            implementation(project(":feature-share:search"))
            implementation(project(":feature-share:lock"))
            implementation(project(":feature-share:notifications"))
            implementation(project(":feature-share:check"))
            implementation(project(":feature-share:fixed-expense"))
            implementation(project(":feature-container:onboarding"))
            implementation(project(":feature-container:dashboard"))
            implementation(project(":feature-container:profile"))
            implementation(project(":feature-container:tools"))
            implementation(project(":feature-share:budget"))
            implementation(project(":feature-share:shopping"))
            implementation(project(":feature-share:sync"))
            implementation(project(":feature-share:backup-export"))
            implementation(project(":feature-share:notes"))
            implementation(project(":feature-share:gamification"))
            implementation(project(":feature-share:sms-reader"))


        }

        iosMain {
            dependencies {
            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        jsMain {
            dependencies {
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.kazemieh.composeApp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kazemieh.fintrack"
            packageVersion = "1.0.0"
        }
    }
}


tasks.register<Copy>("copySqlWasm") {
    // فایل wasm که توی resources/jsMain گذاشتی
    from("src/jsMain/resources/sql-wasm.wasm")
    // پوشهٔ خروجی نهایی webpack (فایل worker اینجاست)
    into("build/js/packages/${project.name}/kotlin/")
}

// اتصال به تسک بیلد JS
tasks.named("jsDevelopmentExecutableCompileSync") {
    finalizedBy("copySqlWasm")
}
// اتصال به تسک بیلد JS
tasks.named("jsBrowserDevelopmentRun") {
    finalizedBy("copySqlWasm")
}