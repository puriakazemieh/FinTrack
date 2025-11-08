plugins {
    id("convention.android.library")
    id("convention.android.serialization")
}

android {
    namespace = "com.kazemieh.common"
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    implementation(libs.jalalicalendar)
    implementation(libs.androidx.annotation.jvm)
}
