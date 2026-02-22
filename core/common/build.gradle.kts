import com.android.build.api.dsl.LibraryExtension

plugins {
    id("convention.android.library")
    id("convention.kotlin.serialization")
}

extensions.configure<LibraryExtension> {
    namespace = "com.kazemieh.common"
}

dependencies {
    implementation(libs.jalalicalendar)
    // implementation(libs.androidx.annotation.jvm)
}