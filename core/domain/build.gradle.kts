plugins {
    id("convention.kotlin.library")
    id("convention.kotlin.serialization")
    id("convention.koin")
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
}