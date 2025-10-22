plugins {
    alias(libs.plugins.kotlin.jvm)
    id("convention.android.serialization")
}


dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(project(":core:model"))
}