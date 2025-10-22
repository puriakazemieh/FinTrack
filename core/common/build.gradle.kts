plugins {
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.kotlin.ksp)

    id("convention.android.library")
    id("convention.android.serialization")
}

android {
    namespace = "com.kazemieh.common"
//    compileSdk = 35
//    defaultConfig { minSdk = 24 }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_21
//        targetCompatibility = JavaVersion.VERSION_21
//    }
//
//    kotlinOptions { jvmTarget = "21" }
}

dependencies {
//    implementation(libs.kotlinx.serialization.json)
//    implementation(libs.kotlinx.datetime)
//    implementation(libs.kotlinx.coroutines.core)
}
