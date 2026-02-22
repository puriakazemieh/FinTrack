plugins {
    id("convention.kotlin.library")
    id("convention.kotlin.serialization")
}


dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:common"))
}