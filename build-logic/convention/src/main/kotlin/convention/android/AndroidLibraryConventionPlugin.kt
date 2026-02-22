package convention.android

import convention.android.internal.configureAndroidCommon
import convention.android.internal.configureKotlinAndroidOptions
import ext.androidLibrary
import ext.implementation
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        configureKotlinAndroidOptions()

        androidLibrary {
            buildFeatures { buildConfig = false }
            configureAndroidCommon(this)
        }

        dependencies {
            implementation(libs.findLibrary("kotlinx-collections-immutable").get())
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("paging-runtime").get())
        }
    }
}