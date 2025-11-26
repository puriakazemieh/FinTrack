plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.tag"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
}