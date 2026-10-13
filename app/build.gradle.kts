// Switches on the plugins this module needs.
plugins {
    // Builds an installable Android app (an APK).
    alias(libs.plugins.android.application)
    // Lets the app be written in Kotlin.
    alias(libs.plugins.kotlin.android)
    // Writes Parcelable code, so an Entity can be passed between screens.
    alias(libs.plugins.kotlin.parcelize)
    // Runs code generators while building. Here that's Hilt's.
    // Why before Hilt: the Hilt plugin looks for KSP and fails without it.
    alias(libs.plugins.ksp)
    // Switches on Hilt for dependency injection.
    alias(libs.plugins.hilt)
    // Generates the "Directions" and "Args" classes from nav_graph.xml.
    // Why "id" not "alias": its version comes from the root build.gradle.kts.
    id("androidx.navigation.safeargs.kotlin")
}

// Android settings for the app.
android {
    // The package for the generated R and BuildConfig classes. Matches the Kotlin package.
    namespace = "com.vu.nit3213app"
    // The Android version the code is compiled against (API 35).
    compileSdk = 35

    // Settings that apply to every build of the app.
    defaultConfig {
        // The app's unique ID on a phone and on the Play Store.
        applicationId = "com.vu.nit3213app"
        // The oldest Android the app runs on: 8.0 (API 26).
        minSdk = 26
        // The Android version the app is tested on. It gets that version's behaviour.
        targetSdk = 35
        // An internal version number, increased with each release.
        versionCode = 1
        // The version name users see.
        versionName = "1.0"

        // The tool that runs on-device tests.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Adds BASE_URL to the generated BuildConfig class, so the code can read the server address.
        // Why the extra quotes: the value is pasted into Java code, so it must be a quoted string.
        // Why the slash at the end: Retrofit refuses a base URL that doesn't end in "/".
        buildConfigField("String", "BASE_URL", "\"https://nit3213apinew.onrender.com/\"")
        // Adds CAMPUS_LOCATION, used in the login path. Can be "footscray", "sydney" or "br".
        buildConfigField("String", "CAMPUS_LOCATION", "\"footscray\"")
    }

    // Settings for each kind of build.
    buildTypes {
        // The release build (the one you'd publish).
        release {
            // Turns off code shrinking.
            // Why: Moshi and Parcelable would need extra rules to survive it, and size doesn't matter here.
            isMinifyEnabled = false
            // The rule files the shrinker would use if it were on.
            proguardFiles(
                // Android's default rules.
                getDefaultProguardFile("proguard-android-optimize.txt"),
                // This project's own rules file.
                "proguard-rules.pro"
            )
        }
    }

    // The Java version for the Java compiler.
    compileOptions {
        // Read the source code as Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        // Produce Java 17 compiled code.
        targetCompatibility = JavaVersion.VERSION_17
    }

    // The Java version for the Kotlin compiler.
    kotlinOptions {
        // Produce Java 17 compiled code. Must match the setting above.
        jvmTarget = "17"
    }

    // Optional build features.
    buildFeatures {
        // Generates a binding class for each XML layout, such as FragmentLoginBinding.
        // Why: it replaces findViewById with safe, typed references to each view.
        viewBinding = true
        // Generates the BuildConfig class that holds BASE_URL and CAMPUS_LOCATION.
        // Why it's needed: it's off by default in newer Android build tools.
        buildConfig = true
    }

    // Settings for tests.
    testOptions {
        // Makes Android methods return default values in unit tests instead of crashing.
        // Why: unit tests run on the computer, where the real Android code isn't available.
        unitTests.isReturnDefaultValues = true
    }
}

// The libraries the app uses.
// implementation() puts a library in the app, ksp() runs a code generator while building,
// and testImplementation() is only for the unit tests, so it never ends up in the app.
dependencies {
    // AndroidX core: Kotlin-friendly helpers such as isVisible.
    implementation(libs.androidx.core.ktx)
    // AppCompat: the toolbar and Activity support.
    implementation(libs.androidx.appcompat)
    // Material Design: the buttons, cards and text fields.
    implementation(libs.material)
    // RecyclerView: the scrolling list on the Dashboard.
    implementation(libs.androidx.recyclerview)
    // Fragment helpers, including "by viewModels()".
    implementation(libs.androidx.fragment.ktx)
    // ViewModel helpers, including viewModelScope.
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Lifecycle helpers, including lifecycleScope and repeatOnLifecycle.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation: moving between Fragments.
    implementation(libs.androidx.navigation.fragment.ktx)
    // Navigation UI: linking the toolbar to the screen changes.
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines for Android, including Dispatchers.Main.
    implementation(libs.kotlinx.coroutines.android)

    // Hilt itself, used while the app runs.
    implementation(libs.hilt.android)
    // Hilt's code generator, which writes the delivery code while building.
    ksp(libs.hilt.compiler)

    // Retrofit: turns NitApiService into working network calls.
    implementation(libs.retrofit)
    // Lets Retrofit use Moshi for JSON.
    implementation(libs.retrofit.converter.moshi)
    // Moshi with Kotlin support.
    implementation(libs.moshi.kotlin)
    // OkHttp's logger, which prints requests and replies in debug builds.
    implementation(libs.okhttp.logging.interceptor)

    // JUnit: runs the unit tests.
    testImplementation(libs.junit)
    // MockK: creates fake objects for the tests.
    testImplementation(libs.mockk)
    // Coroutine test tools, such as runTest.
    testImplementation(libs.kotlinx.coroutines.test)
}
