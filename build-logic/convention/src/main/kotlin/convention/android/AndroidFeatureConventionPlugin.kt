package convention.android

import ext.androidLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        pluginManager.apply("convention.android.library")
        pluginManager.apply("convention.android.compose")


         pluginManager.apply("convention.koin")
         pluginManager.apply("convention.kotlin.serialization")

        androidLibrary {
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
