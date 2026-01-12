plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.setting"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))

    implementation(project(":feature-share:transaction"))
    implementation(project(":feature-share:source"))
    implementation(project(":feature-share:category"))
    implementation(project(":feature-share:tags"))
    implementation(project(":feature-share:person"))
}