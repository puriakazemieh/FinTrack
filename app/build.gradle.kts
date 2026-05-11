plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.kazemieh.fintrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kazemieh.fintrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "2.0.0"
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
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}

afterEvaluate {
    val designSystemProject = project(":core:designsystem")
    val dsProcessedResources =
        File(designSystemProject.buildDir, "processedResources/jvm/main/composeResources")

    android.sourceSets["main"].assets.srcDir(dsProcessedResources.absolutePath)

    tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }
        .configureEach {
            dependsOn(
                designSystemProject.tasks.matching {
                    it.name.equals("copyJvmMainComposeResourcesForAndroid", ignoreCase = true) ||
                            it.name.equals("processJvmMainResources", ignoreCase = true) ||
                            it.name.equals("jvmProcessResources", ignoreCase = true)
                }
            )
        }
}
