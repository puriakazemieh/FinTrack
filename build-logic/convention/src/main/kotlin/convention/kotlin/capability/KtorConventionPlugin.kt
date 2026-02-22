package convention.kotlin.capability

import ext.implementation
import ext.libs
import ext.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.jsoup.nodes.Entities.getByName
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {

        val hasKmp = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
        val hasAndroid = pluginManager.hasPlugin("com.android.application") ||
                pluginManager.hasPlugin("com.android.library") ||
                pluginManager.hasPlugin("com.android.kotlin.multiplatform.library")

        if (hasKmp) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    val commonMain = getByName("commonMain")
                    val commonTest = getByName("commonTest")
                    val androidMain = getByName("androidMain")
                    val iosMain = getByName("iosMain")

                    commonMain.dependencies {
                        implementation(libs.findLibrary("ktor.client.core").get())
                        implementation(libs.findLibrary("ktor.client.content.negotiation").get())
                        implementation(libs.findLibrary("ktor.serialization.kotlinx.json").get())
                    }
                    androidMain.dependencies {
                        implementation(libs.findLibrary("ktor.client.android").get())
                    }
                    iosMain.dependencies {
                        implementation(libs.findLibrary("ktor.client.darwin").get())
                    }
                    commonTest.dependencies {
                        implementation(libs.findLibrary("ktor.client.mock").get())
                    }
                }
            }
            return@with
        }

        // Android-only / JVM-only fallback
        dependencies {
            implementation(libs.findLibrary("ktor.client.core").get())
            implementation(libs.findLibrary("ktor.client.content.negotiation").get())
            implementation(libs.findLibrary("ktor.serialization.kotlinx.json").get())

            if (hasAndroid) implementation(libs.findLibrary("ktor.client.android").get())
        }
    }
}