// Top-level build file. Plugins are declared here with "apply false" so every
// module shares the same plugin versions, then each module applies the ones it needs.
// This project only has one module (:app), but the layout is the standard one
// Android Studio creates, and it keeps plugin versions out of the module file.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Safe Args is added to the build classpath, as shown in the official
        // Navigation documentation and in Lecture 4. The version catalogue is not
        // reliably available inside buildscript {}, so the version is written
        // here and must match "navigation" in gradle/libs.versions.toml.
        // A mismatch can make the generated Directions classes disagree with
        // the runtime library, which shows up as confusing build errors.
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.5")
    }
}

// "apply false" puts each plugin on the build classpath at a fixed version
// without running it on the root project, which has no Android code of its own.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
