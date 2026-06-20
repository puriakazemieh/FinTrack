plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinxSerialization)
    application
}

group = "com.kazemieh"
version = "1.0.0"

application {
    mainClass.set("com.kazemieh.server.ApplicationKt")
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback) // For logging, check if exists in libs.versions.toml

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
