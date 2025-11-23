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

//        versionCode = 1
//        versionName = "1.0.0"
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
    implementation(project(":feature:transaction"))
    implementation(project(":feature:category"))
    implementation(project(":feature:FinancialSource"))
    implementation(project(":feature:Tag"))
    implementation(project(":feature:filter"))
    implementation(project(":feature:person"))

}