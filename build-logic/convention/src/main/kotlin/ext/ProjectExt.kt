package ext

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")


internal inline fun Project.androidApplication(crossinline configure: ApplicationExtension.() -> Unit) =
    extensions.configure<ApplicationExtension> { configure() }

internal inline fun Project.androidLibrary(crossinline configure: LibraryExtension.() -> Unit) =
    extensions.configure<LibraryExtension> { configure() }


internal fun Project.androidAppExtension(): ApplicationExtension =
    extensions.findByType(ApplicationExtension::class.java)
        ?: error("ApplicationExtension not found. Apply 'com.android.application' first.")

internal fun Project.androidLibraryExtension(): LibraryExtension =
    extensions.findByType(LibraryExtension::class.java)
        ?: error("LibraryExtension not found. Apply 'com.android.library' first.")

internal fun Project.warningsAsErrorsProvider(default: Boolean = true): Provider<Boolean> =
    providers.gradleProperty("warningsAsErrors")
        .map { it.toBoolean() }
        .orElse(default)

