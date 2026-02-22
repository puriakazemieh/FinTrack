package convention.android.capability

import ext.implementation
import ext.ksp
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        pluginManager.apply("com.google.devtools.ksp")

        dependencies {
            implementation(libs.findLibrary("room-runtime").get())
            implementation(libs.findLibrary("room-ktx").get())
            implementation(libs.findLibrary("paging-room").get())
            ksp(libs.findLibrary("room-compiler").get())
        }
    }
}
