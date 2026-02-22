import java.net.URI

pluginManagement {
    repositories {
        // اگر repo خصوصی‌ت پلاگین‌ها رو هم mirror می‌کنه، نگهش دار
//        maven { url = uri("https://srepo.tosantechno.net/repository/maven-group/") }

        // این سه تا برای AGP/Kotlin/Gradle plugins ضروری‌اند
        maven {url = uri("https://maven.myket.ir") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {url = uri("https://maven.myket.ir") }
        // repo خصوصی
//        maven { url = uri("https://srepo.tosantechno.net/repository/maven-group/") }

        // این‌ها برای resolve شدن AGP/Kotlin plugin dependencies ضروری‌اند
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")