import modularization.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RoomConvention : Plugin<Project> {

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
            implementation(libs.findLibrary("room.runtime").get())
            implementation(libs.findLibrary("room.ktx").get())
            implementation(libs.findLibrary("paging.room").get())
            annotationProcessor(libs.findLibrary("room.compiler").get())
            ksp(libs.findLibrary("room.compiler").get())
        }
    }


}