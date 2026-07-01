plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.widget"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:designsystem"))
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.glance)
                implementation(libs.androidx.glance.appwidget)
                implementation(libs.androidx.glance.material3)
                implementation(libs.androidx.material3)
                implementation(libs.koin.android)
                implementation(project(":core:preferences"))
            }
        }
    }
}
