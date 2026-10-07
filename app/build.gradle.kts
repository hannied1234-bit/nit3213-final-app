plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Generates Parcelable code so an Entity can be passed between screens.
    alias(libs.plugins.kotlin.parcelize)
    // KSP runs annotation processors (here, Hilt's code generator) at compile time.
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Generates type-safe "Directions" and "Args" classes from the navigation graph.
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.vu.nit3213app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vu.nit3213app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // These values are compiled into the generated BuildConfig class, so the
        // API address and campus are configured in one place instead of being
        // hard-coded throughout the Kotlin code.
        buildConfigField("String", "BASE_URL", "\"https://nit3213api.onrender.com/\"")
        buildConfigField("String", "CAMPUS_LOCATION", "\"footscray\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

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
        buildConfig = true
    }

    testOptions {
        // Android framework methods return default values in local unit tests
        // instead of throwing, which keeps tests focused on our own logic.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // AndroidX and Material Design
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Hilt dependency injection
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
