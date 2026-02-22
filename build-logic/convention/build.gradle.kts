plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

repositories {
    maven {url = uri("https://maven.myket.ir") }
//    maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradleApi)
}


gradlePlugin {
    plugins {

        // --- Android base ---
        register("androidApplicationConvention") {
            id = "convention.android.application"
            implementationClass = "convention.android.AndroidApplicationConventionPlugin"
        }

        register("androidLibraryConvention") {
            id = "convention.android.library"
            implementationClass = "convention.android.AndroidLibraryConventionPlugin"
        }

        register("androidComposeConvention") {
            id = "convention.android.compose"
            implementationClass = "convention.android.AndroidComposeConventionPlugin"
        }

        register("androidFeatureConvention") {
            id = "convention.android.feature"
            implementationClass = "convention.android.AndroidFeatureConventionPlugin"
        }

        // --- Android composite ---
        register("androidApplicationComposeConvention") {
            id = "convention.android.application.compose"
            implementationClass =
                "convention.android.composite.AndroidApplicationComposeConventionPlugin"
        }

        register("androidLibraryComposeConvention") {
            id = "convention.android.library.compose"
            implementationClass =
                "convention.android.composite.AndroidLibraryComposeConventionPlugin"
        }

        // --- Android capabilities ---
        register("androidRoomConvention") {
            id = "convention.android.room"
            implementationClass = "convention.android.capability.RoomConventionPlugin"
        }

        // --- Kotlin/JVM ---
        register("kotlinLibraryConvention") {
            id = "convention.kotlin.library"
            implementationClass = "convention.kotlin.KotlinLibraryConventionPlugin"
        }

        // --- KMP base ---
        register("kotlinMultiplatformConvention") {
            id = "convention.kotlin.multiplatform"
            implementationClass =
                "convention.kotlin.multiplatform.KotlinMultiplatformConventionPlugin"
        }

        // --- KMP Compose (جدید) ---
        register("kotlinMultiplatformComposeConvention") {
            id = "convention.kotlin.multiplatform.compose"
            implementationClass =
                "convention.kotlin.multiplatform.ComposeMultiplatformConventionPlugin"
        }

        // --- Kotlin capabilities ---
        register("kotlinSerializationConvention") {
            id = "convention.kotlin.serialization"
            implementationClass = "convention.kotlin.capability.SerializationConventionPlugin"
        }

        register("kotlinSqlDelightConvention") {
            id = "convention.kotlin.sqldelight"
            implementationClass = "convention.kotlin.capability.SqlDelightConventionPlugin"
        }

        register("koinConvention") {
            id = "convention.koin"
            implementationClass = "convention.kotlin.capability.KoinConventionPlugin"
        }

        register("ktorConvention") {
            id = "convention.ktor"
            implementationClass = "convention.kotlin.capability.KtorConventionPlugin"
        }

    }

}