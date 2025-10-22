import modularization.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConvention : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            applyPlugins()
            applyDependencies()
        }
    }

    private fun Project.applyPlugins() {
        pluginManager.apply {
            apply("com.google.devtools.ksp")
        }
    }

    private fun Project.applyDependencies() {
        dependencies {
            implementation( libs.findLibrary("koin.core").get())
            implementation( libs.findLibrary("koin.android").get())
            implementation( libs.findLibrary("koin.compose").get())
        }
    }
}