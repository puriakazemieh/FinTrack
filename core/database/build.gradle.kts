plugins {
    id("convention.android.library")
    id("convention.android.serialization")
    id("convention.android.koin")
    id("convention.android.room")
    id("androidx.room") version ("2.6.1")
}

android {
    namespace = "com.kazemieh.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data-contract"))

}
