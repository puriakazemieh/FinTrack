plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.kazemieh.fintrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kazemieh.fintrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "5.0.0"
    }

    signingConfigs {
        create("release") {
            // این مقادیر باید در gradle.properties یا محیط CI تعریف شوند
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "dummy_password"
            keyAlias = System.getenv("KEY_ALIAS") ?: "dummy_alias"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "dummy_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
            // می‌توان تنظیمات خاص گوگل پلی را اینجا اضافه کرد
        }
        create("direct") {
            dimension = "distribution"
//            versionNameSuffix = "-direct"
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
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":feature-share:notifications"))
    implementation(project(":feature-share:widget"))
    implementation(project(":feature-share:sync"))
    implementation(project(":feature-share:fixed-expense"))


    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
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
