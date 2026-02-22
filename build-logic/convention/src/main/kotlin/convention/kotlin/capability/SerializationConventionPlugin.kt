package convention.kotlin.capability

import ext.implementation
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


class SerializationConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        val hasKmp = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
        if (hasKmp) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.findLibrary("kotlinx.serialization.json").get())
                }
            }
        } else {
            dependencies {
                implementation(libs.findLibrary("kotlinx.serialization.json").get())
            }
        }
    }
}