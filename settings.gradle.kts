// Tells Gradle where to download plugins (pluginManagement) and libraries
// (dependencyResolutionManagement) from, and which modules make up the project.
// Gradle reads this file first, before any build.gradle.kts.
pluginManagement {
    repositories {
        // Google's repository is limited to Google and AndroidX groups. Gradle
        // then skips it when looking up other plugins, which speeds up sync and
        // stops a lookalike package being pulled from the wrong repository.
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
    // Forces every module to use the repositories declared here, so there is a
    // single source of truth for where dependencies come from.
    // A module that tried to add its own repositories {} block would fail the
    // build instead of quietly downloading from somewhere unexpected.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NIT3213FinalApp"
// The app is a single module. A larger project might split data, domain and
// UI into separate modules and list each one here.
include(":app")
