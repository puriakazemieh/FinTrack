plugins {
    id("convention.android.library")
    id("convention.kotlin.serialization")
}


android {
    namespace = "com.kazemieh.data_contract"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:common"))
}