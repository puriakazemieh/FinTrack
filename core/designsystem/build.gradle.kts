plugins {
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.designsystem"
//    kotlinOptions {
//        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
//    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.jalali.datepicker.compose)
    implementation(libs.jalalicalendar)
}
