package convention.kotlin.multiplatform

import ext.implementation
import ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {

        // این پلاگین فقط معنی دارد وقتی ماژول KMP باشد
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {

            // Compose Multiplatform + Kotlin Compose Compiler plugin
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val kotlin = extensions.getByType<KotlinMultiplatformExtension>()

            kotlin.sourceSets.getByName("commonMain").dependencies {
                // bundle جدیدی که پایین در toml می‌سازیم
//                api(libs.findLibrary("compose.bom").get())
                implementation(libs.findBundle("compose.multiplatform").get())
            }
        }
    }
}
