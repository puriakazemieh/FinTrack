package convention.android.composite


import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("convention.android.application")
        pluginManager.apply("convention.android.compose")
    }
}
