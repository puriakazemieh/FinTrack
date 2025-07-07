pluginManagement {
    repositories {
//        maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "FinTrack"
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:model")
include(":feature:transaction")
include(":core:domain")
include(":core:data-contract")
