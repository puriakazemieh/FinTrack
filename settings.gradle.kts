pluginManagement {
    repositories {
//        includeBuild("build-logic")
//        maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
        maven {url = uri("https://maven.myket.ir") }
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        mavenCentral()
//        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven {url = uri("https://maven.myket.ir") }
//        maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
//        google()
//        mavenCentral()
        ivy {
            name = "Node Distributions"
            url = uri("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
    }
}

rootProject.name = "FinTrack"
//include(":app")
include(":core:common")
include(":core:data")
include(":core:data-contract")
include(":core:database")
include(":core:designsystem")
include(":core:domain")
include(":feature-share:transaction")
include(":feature-share:category")
include(":feature-share:source")
include(":feature-share:tags")
include(":feature-share:person")

include(":feature-container:report")
include(":feature-container:dashboard")
include(":feature-container:setting")
