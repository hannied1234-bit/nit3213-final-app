plugins {
    // Builds an installable APK, unlike a library module, which builds an AAR.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Generates Parcelable code so an Entity can be passed between screens.
    alias(libs.plugins.kotlin.parcelize)
    // KSP runs annotation processors (here, Hilt's code generator) at compile time.
    // KSP has to be applied before Hilt, because the Hilt plugin looks for it
    // and fails if it isn't there.
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Generates type-safe "Directions" and "Args" classes from the navigation graph.
    // It's applied by id, not alias, because its version comes from the
    // buildscript classpath in the root build.gradle.kts.
    id("androidx.navigation.safeargs.kotlin")
}

android {
    // The package used for the generated R and BuildConfig classes. It must
    // match the Kotlin package of the source code.
    namespace = "com.vu.nit3213app"
    // The Android API level the code is compiled against. It decides which
    // framework classes and methods the compiler knows about.
    compileSdk = 35

    defaultConfig {
        // The app's unique ID on a device and on the Play Store. Changing it
        // later would install as a separate app.
        applicationId = "com.vu.nit3213app"
        // Android 8.0. Phones older than this can't install the app.
        minSdk = 26
        // Tells Android the app has been tested on API 35, so it gets that
        // version's behaviour, such as edge-to-edge drawing by default.
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // These values are compiled into the generated BuildConfig class, so the
        // API address and campus are configured in one place instead of being
        // hard-coded throughout the Kotlin code.
        // The escaped quotes are needed because the value is pasted into
        // generated Java source as written, so it has to be a valid string literal.
        // The spec gives the base URL without a trailing slash, but Retrofit
        // refuses a base URL that doesn't end in "/", so one is added here.
        buildConfigField("String", "BASE_URL", "\"https://nit3213apinew.onrender.com/\"")
        // The campus part of the auth path: "footscray", "sydney" or "br".
        buildConfigField("String", "CAMPUS_LOCATION", "\"footscray\"")
    }

    buildTypes {
        release {
            // R8 shrinking is off. Moshi's reflection-based adapter and the
            // Parcelable classes would need keep rules to survive shrinking,
            // and an assignment build doesn't need a smaller APK.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java 17 bytecode for both the Java and Kotlin compilers. The two
    // settings have to agree, or Gradle stops the build with a JVM target
    // mismatch error.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        // ViewBinding generates a binding class for each XML layout, which
        // replaces findViewById with null-safe, type-safe view references.
        viewBinding = true
        // BuildConfig generation is off by default since AGP 8, so it's turned
        // back on for the BASE_URL and CAMPUS_LOCATION fields above.
        buildConfig = true
    }

    testOptions {
        // Android framework methods return default values in local unit tests
        // instead of throwing, which keeps tests focused on our own logic.
        // The android.jar used for JVM tests contains stubs that throw
        // "Method not mocked" by default. This turns those calls into no-ops.
        unitTests.isReturnDefaultValues = true
    }
}

// implementation() puts a library in the app. ksp() runs a code generator at
// compile time only. testImplementation() is only available to src/test, so
// MockK and friends never end up in the APK.
dependencies {
    // AndroidX and Material Design
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    // Provides the "by viewModels()" delegate used in the Fragments.
    implementation(libs.androidx.fragment.ktx)
    // Provides viewModelScope.
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Provides lifecycleScope and repeatOnLifecycle.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines
    // The Android artifact adds Dispatchers.Main, backed by the main thread.
    implementation(libs.kotlinx.coroutines.android)

    // Hilt dependency injection
    // hilt-android is the runtime library. hilt-compiler generates the
    // dependency graph during the build.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Networking: Retrofit for HTTP calls, Moshi for JSON, OkHttp for logging
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp.logging.interceptor)

    // Local unit tests (run on the JVM, no emulator needed)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
