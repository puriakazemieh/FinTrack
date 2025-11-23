plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.person"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
}