package modularization

import com.android.build.api.dsl.CommonExtension
import debugImplementation
import implementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.composeGradleExtension(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {

            val bom = libs.findLibrary("androidx.compose.bom").get()
            implementation(platform(bom))

//            implementation(libs.findLibrary("androidx.activity.compose").get())
            implementation(libs.findLibrary("androidx.ui").get())
            implementation(libs.findLibrary("androidx.ui.graphics").get())
            implementation(libs.findLibrary("androidx.compose.runtime").get())

            implementation(libs.findLibrary("androidx.ui.tooling").get())
            implementation(libs.findLibrary("androidx.ui.tooling.preview").get())
            implementation(libs.findLibrary("androidx.material3").get())
            implementation(libs.findLibrary("coil.compose").get())
            implementation(libs.findLibrary("constraintlayout.compose").get())
            implementation(libs.findLibrary("androidx.compose.material.icons.core").get())
            implementation(libs.findLibrary("androidx.compose.material.icons.extended").get())

            debugImplementation(libs.findLibrary("androidx.ui.tooling").get())
            debugImplementation(libs.findLibrary("androidx.ui.tooling.preview").get())
            debugImplementation(libs.findLibrary("androidx.ui.test.manifest").get())
        }

    }

}