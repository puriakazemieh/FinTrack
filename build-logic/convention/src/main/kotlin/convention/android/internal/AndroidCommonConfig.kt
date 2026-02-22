package convention.android.internal

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import config.AndroidConfig
import ext.coreLibraryDesugaring
import ext.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies


internal fun Project.configureAndroidCommon(app: ApplicationExtension) = with(app) {
    compileSdk = AndroidConfig.COMPILE_SDK
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.apply {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }

    packaging.apply {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    dependencies {
        coreLibraryDesugaring(libs.findLibrary("android-desugarJdkLibs").get())
    }
}

internal fun Project.configureAndroidCommon(app: LibraryExtension) = with(app) {
    compileSdk = AndroidConfig.COMPILE_SDK
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.apply {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }

    packaging.apply {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    dependencies {
        coreLibraryDesugaring(libs.findLibrary("android-desugarJdkLibs").get())
    }
}