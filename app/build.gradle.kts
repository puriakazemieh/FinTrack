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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {

    implementation(libs.navigation.compose)

    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
    implementation(project(":feature:transaction"))
    implementation(project(":feature:category"))
    implementation(project(":feature:FinancialSource"))
    implementation(project(":feature:Tag"))

}