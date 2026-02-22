package convention.android

import convention.android.internal.configureAndroidApplicationDefaults
import convention.android.internal.configureAndroidCommon
import convention.android.internal.configureKotlinAndroidOptions
import ext.androidApplication
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        configureKotlinAndroidOptions()

        androidApplication {
            configureAndroidCommon(this)
            configureAndroidApplicationDefaults(this)
        }
    }
}
