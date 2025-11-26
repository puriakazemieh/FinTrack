plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.financialsource"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
}