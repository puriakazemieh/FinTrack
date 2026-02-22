plugins {
    id("convention.kotlin.library")
    id("convention.kotlin.serialization")
}

dependencies {
    implementation(libs.persianDateTime)
    implementation(libs.kotlinx.datetime)
}