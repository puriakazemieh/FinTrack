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
        versionCode = 11
        versionName = "5.1.0"
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

    firebaseCrashlytics {
        mappingFileUploadEnabled = true
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

/**
 * Compose resources from KMP libraries are runtime assets on Android. A normal Android app does
 * not merge them automatically, so copy both generated trees before every variant's asset merge.
 */
val copyKmpComposeResourcesForAndroid = tasks.register<org.gradle.api.tasks.Copy>("copyKmpComposeResourcesForAndroid") {
    dependsOn(
        ":composeApp:jvmProcessResources",
        ":core:designsystem:jvmProcessResources",
    )
    from(project(":composeApp").layout.buildDirectory.dir("processedResources/jvm/main"))
    from(project(":core:designsystem").layout.buildDirectory.dir("processedResources/jvm/main"))
    into(layout.buildDirectory.dir("generated/kmpComposeResources/android"))
}

android.sourceSets["main"].assets.directories.add(
    layout.buildDirectory.dir("generated/kmpComposeResources/android")
)

tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("Assets")) {
        dependsOn(copyKmpComposeResourcesForAndroid)
    }
}
