// Top-level build file. Plugins are declared here with "apply false" so every
// module shares the same plugin versions, then each module applies the ones it needs.

// Settings for the build process itself.
buildscript {
    // Where to download the build tools from.
    repositories {
        // Google's repository.
        google()
        // Maven Central.
        mavenCentral()
    }
    // Tools the build needs.
    dependencies {
        // Adds the Safe Args plugin, which generates the classes that pass values between screens.
        // Why the version is written here: the version catalogue isn't reliably available inside
        // buildscript {}, so it must be kept in step with "navigation" in gradle/libs.versions.toml.
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.5")
    }
}

// Declares the plugins and their versions for the whole project.
// "apply false" means: make it available, but don't switch it on here. The app module does that.
plugins {
    // The Android app plugin, which builds an installable app.
    alias(libs.plugins.android.application) apply false
    // The Kotlin plugin for Android.
    alias(libs.plugins.kotlin.android) apply false
    // The Parcelize plugin, which writes Parcelable code for us.
    alias(libs.plugins.kotlin.parcelize) apply false
    // KSP, which runs code generators such as Hilt's.
    alias(libs.plugins.ksp) apply false
    // The Hilt plugin.
    alias(libs.plugins.hilt) apply false
}
