import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

//compose.resources {
//    publicResClass = true
//    packageOfResClass = "com.kazemieh.composeApp.generated.resources"
//}

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
            implementation(libs.koin.core)


            implementation(libs.navigation.compose)
            implementation(project(":core:common"))
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:database"))
            implementation(project(":core:domain"))
            implementation(project(":feature-share:transaction"))
            implementation(project(":feature-share:category"))
            implementation(project(":feature-share:source"))
            implementation(project(":feature-share:tags"))
            implementation(project(":feature-container:report"))
            implementation(project(":feature-share:person"))
            implementation(project(":feature-container:dashboard"))
            implementation(project(":feature-container:setting"))


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
