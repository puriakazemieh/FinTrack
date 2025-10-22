import java.net.URI

dependencyResolutionManagement {
    repositories {
        maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
//        google()
//        maven { url = URI("https://jitpack.io") }
//        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "build-logic"
include(":convention")
