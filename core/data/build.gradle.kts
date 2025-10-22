plugins {
    id("convention.android.library")
    id("convention.android.serialization")
    id("convention.android.koin")
}

android {
    namespace = "com.kazemieh.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data-contract"))
}