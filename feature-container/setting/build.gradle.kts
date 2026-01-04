plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.dashboard"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))

    implementation(project(":feature-share:transaction"))
    implementation(project(":feature-share:source"))
}