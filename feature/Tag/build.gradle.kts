plugins {
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
    id("convention.android.feature")
}

android {
    namespace = "com.kazemieh.tag"
//    compileSdk = 35
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_21
//        targetCompatibility = JavaVersion.VERSION_21
//    }
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
//    implementation(libs.androidx.compose.material.icons.core)
//    implementation(libs.androidx.compose.material.icons.extended)

    // Coroutines
//    implementation(libs.kotlinx.coroutines.android)

//    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))

//    implementation(libs.koin.android)
//    implementation(libs.koin.compose)
//    implementation(libs.koin.navigation)
}