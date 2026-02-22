package convention.kotlin.multiplatform


import config.AndroidConfig
import ext.androidLibrary
import ext.warningsAsErrorsProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(21)
            androidLibrary {
                compileSdk = AndroidConfig.COMPILE_SDK

                defaultConfig {
                    minSdk = AndroidConfig.MIN_SDK
                }

            }

            // iOS targets
            iosX64()
            iosArm64()
            iosSimulatorArm64()

            targets.withType(KotlinNativeTarget::class.java).configureEach {
                binaries.framework { baseName = project.name }
            }

            // ✅ تنظیم jvmTarget را روی Android compilations انجام می‌دهیم
            targets.withType(KotlinAndroidTarget::class.java).configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                    allWarningsAsErrors.set(warningsAsErrorsProvider(default = true))
                    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                }
            }

            // sourceSets
            sourceSets.apply {
                val commonMain = getByName("commonMain")
                val commonTest = getByName("commonTest")

                val iosMain = maybeCreate("iosMain").apply { dependsOn(commonMain) }
                val iosTest = maybeCreate("iosTest").apply { dependsOn(commonTest) }

                getByName("iosX64Main").dependsOn(iosMain)
                getByName("iosArm64Main").dependsOn(iosMain)
                getByName("iosSimulatorArm64Main").dependsOn(iosMain)

                getByName("iosX64Test").dependsOn(iosTest)
                getByName("iosArm64Test").dependsOn(iosTest)
                getByName("iosSimulatorArm64Test").dependsOn(iosTest)
            }
        }
    }
}
