plugins {
//    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.kotlin.serialization)
//    alias(libs.plugins.kotlin.ksp)
    id("convention.android.library")
    id("convention.android.serialization")
    id("convention.android.koin")
}

//kotlin {
//    jvmToolchain(21)
//}

android {
    namespace = "com.kazemieh.domain"
}
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
//    implementation(libs.koin.core)
//    implementation(libs.kotlinx.coroutines.core)
}