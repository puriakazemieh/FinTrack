plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.designsystem"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.jalali.datepicker.compose)
    implementation(libs.jalalicalendar)
}
