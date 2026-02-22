plugins {
    id("convention.kotlin.library")
    id("convention.kotlin.serialization")
    id("convention.koin")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:data-contract"))
    implementation(libs.kotlinx.coroutines.core)
}