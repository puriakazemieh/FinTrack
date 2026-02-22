package convention.android.composite


import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("convention.android.library")
        pluginManager.apply("convention.android.compose")
    }
}
