// Tells Gradle where to download plugins and libraries from, and which modules make up the project.
// Gradle reads this file first, before any build.gradle.kts.

// Settings for where build plugins come from.
pluginManagement {
    // The places to look for plugins, in order.
    repositories {
        // Google's repository, which holds the Android plugins.
        google {
            // Only looks here for these groups.
            // Why: Gradle skips Google for everything else, which makes syncing faster.
            content {
                // Android build tools.
                includeGroupByRegex("com\\.android.*")
                // Google libraries.
                includeGroupByRegex("com\\.google.*")
                // AndroidX libraries.
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central, the main public library repository.
        mavenCentral()
        // Gradle's own plugin repository.
        gradlePluginPortal()
    }
}

// Settings for where app libraries (like Retrofit and Hilt) come from.
dependencyResolutionManagement {
    // Stops any module adding its own repositories.
    // Why: one list here means dependencies can't quietly come from somewhere unexpected.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // The places to look for libraries.
    repositories {
        // Google's repository, for AndroidX and Material.
        google()
        // Maven Central, for Retrofit, Moshi, OkHttp, MockK and others.
        mavenCentral()
    }
}

// The project's name, as shown in Android Studio.
rootProject.name = "NIT3213FinalApp"
// Includes the "app" module, which holds all the code.
include(":app")
