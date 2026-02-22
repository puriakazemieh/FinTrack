package convention.android.internal

import com.android.build.api.dsl.ApplicationExtension
import config.AndroidConfig
import org.gradle.api.Project

internal fun Project.configureAndroidApplicationDefaults(app: ApplicationExtension) = with(app) {
    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
    }

    buildFeatures {
        buildConfig = true
    }
}
