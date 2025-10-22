plugins {
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.kotlin.ksp)
    id("convention.android.library")
    id("convention.android.serialization")
    id("convention.android.koin")
}

android {
    namespace = "com.kazemieh.data"
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
//    implementation(libs.kotlinx.coroutines.core)


    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data-contract"))

//    implementation(libs.koin.core)
}