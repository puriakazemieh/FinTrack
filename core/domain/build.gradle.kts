plugins {
    id("convention.android.library")
    id("convention.android.serialization")
    id("convention.android.koin")
}

android {
    namespace = "com.kazemieh.domain"
}
dependencies {
    implementation(project(":core:common"))
}