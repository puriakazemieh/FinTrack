plugins {
    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.kotlin.serialization)
//    id("convention.android.library")
    id("convention.android.serialization")
}

//android {
//    namespace = "com.kazemieh.data_contract"
//}
//kotlin {
//    jvmToolchain(21)
//}

dependencies {

//    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:model"))
}