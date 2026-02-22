package ext//@file:Suppress("UNCHECKED_CAST")
//
//package ext
//
//import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
//import org.gradle.api.Action
//import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
//
//internal fun KotlinMultiplatformExtension.androidKmpLibrary(
//    configure: KotlinMultiplatformAndroidLibraryTarget.() -> Unit
//): KotlinMultiplatformAndroidLibraryTarget {
//    val method =
//        findSingleArgMethod("android")      // جدیدتر
//            ?: findSingleArgMethod("androidLibrary") // قدیمی‌تر
//            ?: error(
//                "Neither kotlin.android{} nor kotlin.androidLibrary{} was found. " +
//                        "Make sure 'com.android.kotlin.multiplatform.library' plugin is applied."
//            )
//
//    return try {
//        method.invoke(this, configure as Any) as KotlinMultiplatformAndroidLibraryTarget
//    } catch (_: IllegalArgumentException) {
//        val action = Action<KotlinMultiplatformAndroidLibraryTarget> { it.configure() }
//        method.invoke(this, action) as KotlinMultiplatformAndroidLibraryTarget
//    }
//}
//
//private fun KotlinMultiplatformExtension.findSingleArgMethod(name: String) =
//    this::class.java.methods.firstOrNull { it.name == name && it.parameterTypes.size == 1 }
