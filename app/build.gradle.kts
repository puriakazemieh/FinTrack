plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.kazemieh.fintrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kazemieh.fintrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "2.5.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature-share:notifications"))
    implementation(project(":feature-share:sync"))
    implementation(project(":feature-share:fixed-expense"))


    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}

afterEvaluate {
    // ---------- :composeApp ----------
    val composeAppProject = project(":composeApp")
    val composeParentResources =
        File(composeAppProject.buildDir, "processedResources/jvm/main")
    android.sourceSets["main"].assets.srcDir(composeParentResources.absolutePath)

    tasks.matching { 
        (it.name.startsWith("merge") && it.name.endsWith("Assets")) ||
        it.name.contains("Lint", ignoreCase = true)
    }
        .configureEach {
            dependsOn(
                composeAppProject.tasks.matching {
                    it.name.equals("copyJvmMainComposeResourcesForAndroid", ignoreCase = true) ||
                            it.name.equals("processJvmMainResources", ignoreCase = true) ||
                            it.name.equals("jvmProcessResources", ignoreCase = true)
                }
            )
        }

    // ---------- :core:designsystem ----------
    val designSystemProject = project(":core:designsystem")
    val designSystemParentResources =
        File(designSystemProject.buildDir, "processedResources/jvm/main")
    android.sourceSets["main"].assets.srcDir(designSystemParentResources.absolutePath)

    tasks.matching { 
        (it.name.startsWith("merge") && it.name.endsWith("Assets")) ||
        it.name.contains("Lint", ignoreCase = true)
    }
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