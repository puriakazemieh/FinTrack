plugins {
    id("convention.android.library")
    id("convention.android.serialization")
}

android {
    namespace = "com.kazemieh.common"
}

dependencies {
    implementation(libs.jalalicalendar)
}
