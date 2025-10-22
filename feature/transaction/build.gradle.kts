plugins {
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.transaction"
//    compileSdk = 35
//    compileSdk = 35

//    defaultConfig {
//        minSdk = 24
//        targetSdk = 35
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_21
//        targetCompatibility = JavaVersion.VERSION_21
//    }
//
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.12"
//    }
//
//    kotlinOptions {
//        jvmTarget = "21"
//    }
}

dependencies {



    // Compose
//    implementation(libs.androidx.compose.ui)
//    implementation(libs.androidx.compose.material3)
//    implementation(libs.androidx.compose.runtime)
//    implementation(libs.androidx.lifecycle.viewmodel.compose)
//    implementation(libs.androidx.navigation.compose)
//    implementation(platform(libs.androidx.compose.bom))
//
//    // Coroutines
//    implementation(libs.kotlinx.coroutines.android)
//
//    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))

    implementation(libs.jalali.datepicker.compose)
    implementation(libs.jalalicalendar)

//    implementation(libs.koin.android)
//    implementation(libs.koin.compose)
//    implementation(libs.koin.navigation)

    implementation(libs.androidx.foundation)



}