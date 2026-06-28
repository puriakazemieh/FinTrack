pluginManagement {
    repositories {
//        includeBuild("build-logic")
//        maven {url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
//        maven {url = uri("https://maven.myket.ir") }
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
//       maven {url = uri("https://maven.myket.ir") }
//        maven { url = uri("https://srepo.tosantechno.net/repository/maven-group/") }
        google()
        mavenCentral()
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
include(":app")
include(":composeApp")
include(":core:common")
include(":core:storage")
include(":core:data")
include(":core:data-contract")
include(":core:database")
include(":core:designsystem")
include(":core:domain")
include(":core:preferences")
include(":core:money")
include(":core:jalali")
include(":core:network")
include(":feature-share:asset")
include(":feature-share:transaction")
include(":feature-share:category")
include(":feature-share:source")
include(":feature-share:tags")
include(":feature-share:person")
include(":feature-share:ai-insights")
include(":feature-share:debt")
include(":feature-share:search")
include(":feature-share:lock")
include(":feature-share:notifications")
include(":feature-share:budget")
include(":feature-share:installment")
include(":feature-share:check")
include(":feature-share:fixed-expense")
include(":feature-share:shopping")
include(":feature-share:notes")
include(":feature-share:sms-reader")
include(":feature-share:backup-export")
include(":feature-share:sync")
include(":server")

include(":feature-container:transactions")
include(":feature-container:onboarding")
include(":feature-container:dashboard")
include(":feature-container:profile")
include(":feature-container:tools")
