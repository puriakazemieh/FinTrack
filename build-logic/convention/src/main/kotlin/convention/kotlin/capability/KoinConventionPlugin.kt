package convention.kotlin.capability


import ext.implementation
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {

        val hasKmp = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
        val hasAndroid = pluginManager.hasPlugin("com.android.application") ||
                pluginManager.hasPlugin("com.android.library") ||
                pluginManager.hasPlugin("com.android.kotlin.multiplatform.library")

        if (hasKmp) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    val commonMain = getByName("commonMain")
                    val androidMain = getByName("androidMain")

                    commonMain.dependencies {
                        implementation(libs.findLibrary("koin.core").get())
                        implementation(libs.findLibrary("koin.compose").get())
                    }
                    androidMain.dependencies {
                        implementation(libs.findLibrary("koin.android").get())
                        implementation(libs.findLibrary("koin.androidx.compose").get())
                    }
                }
            }
            return@with
        }

        // Android-only / JVM-only fallback
        dependencies {
            implementation(libs.findLibrary("koin.core").get())
            if (hasAndroid) {
                implementation(libs.findLibrary("koin.android").get())
                implementation(libs.findLibrary("koin.androidx.compose").get())
            }
        }
    }
}