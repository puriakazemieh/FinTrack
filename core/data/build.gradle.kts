plugins {
    id("convention.android.library")
    id("convention.kotlin.serialization")
    id("convention.koin")
}

android {
    namespace = "com.kazemieh.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:data-contract"))
}