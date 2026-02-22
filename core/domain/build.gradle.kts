plugins {
    id("convention.android.library")
    id("convention.kotlin.serialization")
    id("convention.koin")
}

android {
    namespace = "com.kazemieh.domain"
}
dependencies {
    implementation(project(":core:common"))
}