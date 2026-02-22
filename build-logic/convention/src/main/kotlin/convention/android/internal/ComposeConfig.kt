package convention.android.internal

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import ext.debugImplementation
import ext.implementation
import ext.implementationBundle
import ext.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureCompose(common: ApplicationExtension) = with(common) {
    buildFeatures { compose = true }

    dependencies {
        implementation(platform(libs.findLibrary("androidx-compose-bom").get()))
        implementationBundle(libs.findBundle("compose").get())
        debugImplementation(libs.findBundle("composeDebug").get())
    }
}


internal fun Project.configureCompose(lib: LibraryExtension) = with(lib) {
    buildFeatures.apply { compose = true }

    dependencies {
        implementation(platform(libs.findLibrary("androidx-compose-bom").get()))
        implementationBundle(libs.findBundle("compose").get())
        debugImplementation(libs.findBundle("composeDebug").get())
    }
}