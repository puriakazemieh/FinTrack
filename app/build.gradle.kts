plugins {
    id("convention.android.application")
    id("convention.android.koin")
    id("convention.android.application.compose")
    id("convention.android.serialization")
}

android {
    namespace = "com.kazemieh.fintrack"

    defaultConfig {
        applicationId = "com.kazemieh.fintrack"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
}

dependencies {

    implementation(libs.navigation.compose)

    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
    implementation(project(":feature-share:transaction"))
    implementation(project(":feature-share:category"))
    implementation(project(":feature-share:source"))
    implementation(project(":feature-share:tags"))
    implementation(project(":feature-share:filter"))
    implementation(project(":feature-share:person"))
    implementation(project(":feature-container:dashboard"))

}