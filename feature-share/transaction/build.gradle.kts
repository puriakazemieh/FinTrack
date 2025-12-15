plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.transaction"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature-share:source"))
    implementation(project(":feature-share:category"))
    implementation(project(":feature-share:tags"))
    implementation(project(":feature-share:person"))
    implementation(libs.androidx.foundation)
}