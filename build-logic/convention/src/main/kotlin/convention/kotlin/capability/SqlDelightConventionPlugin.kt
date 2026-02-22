package convention.kotlin.capability

import ext.implementation
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


class SqlDelightConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("app.cash.sqldelight")

        val hasKmp = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
        val hasAndroid = pluginManager.hasPlugin("com.android.application") ||
                pluginManager.hasPlugin("com.android.library") ||
                pluginManager.hasPlugin("com.android.kotlin.multiplatform.library")
        if (hasKmp) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    getByName("commonMain").dependencies {
                        implementation(libs.findLibrary("sqldelight.runtime").get())
                    }
                    getByName("androidMain").dependencies {
                        implementation(libs.findLibrary("sqldelight.android.driver").get())
                    }
                    getByName("iosMain").dependencies {
                        implementation(libs.findLibrary("sqldelight.native.driver").get())
                    }
                }
            }
            return@with
        }

        dependencies {
            implementation(libs.findLibrary("sqldelight.runtime").get())
            if (hasAndroid) implementation(libs.findLibrary("sqldelight.android.driver").get())
        }

    }
}