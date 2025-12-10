plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.category"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
}