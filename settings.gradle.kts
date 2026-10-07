// Tells Gradle where to download plugins (pluginManagement) and libraries
// (dependencyResolutionManagement) from, and which modules make up the project.
pluginManagement {
    repositories {
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NIT3213FinalApp"
include(":app")
