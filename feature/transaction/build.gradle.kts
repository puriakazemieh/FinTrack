plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.transaction"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))

    implementation(libs.jalali.datepicker.compose)
    implementation(libs.jalalicalendar)

    implementation(libs.androidx.foundation)



}