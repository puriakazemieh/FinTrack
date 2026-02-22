package convention.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import convention.android.internal.configureCompose
import org.gradle.api.Plugin
import org.gradle.api.Project


class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {

        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.findByType(ApplicationExtension::class.java)?.let { app ->
            configureCompose(app)
            return@with
        }

        extensions.findByType(LibraryExtension::class.java)?.let { lib ->
            configureCompose(lib)
            return@with
        }

        error("Android extension not found. Apply 'com.android.application' or 'com.android.library' first.")
    }
}