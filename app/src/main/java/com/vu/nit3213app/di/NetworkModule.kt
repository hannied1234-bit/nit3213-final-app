// Says which folder (package) this file belongs to.
package com.vu.nit3213app.di

// Moshi: the library that converts between JSON and Kotlin objects.
import com.squareup.moshi.Moshi
// KotlinJsonAdapterFactory: teaches Moshi how Kotlin data classes work.
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
// BuildConfig: generated class holding BASE_URL and CAMPUS_LOCATION from app/build.gradle.kts.
import com.vu.nit3213app.BuildConfig
// NitApiService: the Retrofit description of the server's endpoints.
import com.vu.nit3213app.data.remote.NitApiService
// Module: marks a class as a set of Hilt instructions.
import dagger.Module
// Provides: marks a function that tells Hilt how to make something.
import dagger.Provides
// InstallIn: says how long the things this module makes should live.
import dagger.hilt.InstallIn
// SingletonComponent: Hilt's container that lives as long as the whole app.
import dagger.hilt.components.SingletonComponent
// OkHttpClient: the library that actually sends HTTP requests.
import okhttp3.OkHttpClient
// HttpLoggingInterceptor: prints requests and replies to Logcat.
import okhttp3.logging.HttpLoggingInterceptor
// Retrofit: turns our NitApiService interface into working network code.
import retrofit2.Retrofit
// MoshiConverterFactory: lets Retrofit use Moshi for JSON.
import retrofit2.converter.moshi.MoshiConverterFactory
// TimeUnit: lets us say "seconds" for the timeouts.
import java.util.concurrent.TimeUnit
// Singleton: the label meaning "make only one and share it".
import javax.inject.Singleton

/**
 * Hilt module that tells Hilt how to build the networking objects.
 *
 * These come from third-party libraries (we cannot add @Inject to their
 * constructors), so each one gets a @Provides function instead.
 *
 * The functions form a chain. provideRetrofit() asks for an OkHttpClient and a
 * Moshi, and Hilt answers by calling the two functions above it. Hilt works out
 * the order from the parameter types when the app is built.
 */
// Marks this as a set of Hilt instructions.
@Module
// Says everything made here lives as long as the app.
@InstallIn(SingletonComponent::class)
// Creates the module. "object" means there's only ever one, with no set-up needed.
object NetworkModule {

    // How many seconds to wait for the server before giving up.
    // Why 60: the free Render server sleeps when idle and can take up to a minute to wake.
    private const val TIMEOUT_SECONDS = 60L

    // Tells Hilt how to make the campus String.
    @Provides
    // Tags it, so it goes only to parameters with the same @CampusLocation tag.
    @CampusLocation
    // Returns the campus from app/build.gradle.kts, such as "footscray".
    fun provideCampusLocation(): String = BuildConfig.CAMPUS_LOCATION

    // Tells Hilt how to make Moshi.
    @Provides
    // Makes only one Moshi and shares it.
    @Singleton
    // Starts building a Moshi.
    fun provideMoshi(): Moshi = Moshi.Builder()
        // Adds support for Kotlin data classes, including default values.
        .add(KotlinJsonAdapterFactory())
        // Finishes building it.
        .build()

    // Tells Hilt how to make the OkHttpClient.
    @Provides
    // Makes only one and shares it.
    // Why: OkHttp reuses connections, so one shared client is faster.
    @Singleton
    // The function that builds the client.
    fun provideOkHttpClient(): OkHttpClient {
        // Creates the logger and sets how much it prints.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Picks the logging level based on the build type.
            level = if (BuildConfig.DEBUG) {
                // Debug build: print everything, including the JSON. Handy for seeing your topic's data.
                HttpLoggingInterceptor.Level.BODY
            // Release build.
            } else {
                // Print nothing.
                // Why: the login body contains your details and shouldn't be logged.
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Starts building the client.
        return OkHttpClient.Builder()
            // Adds the logger, so every request passes through it.
            .addInterceptor(loggingInterceptor)
            // Waits up to 60 seconds to connect to the server.
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Waits up to 60 seconds for the server's reply.
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Waits up to 60 seconds to send our request.
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Finishes building it.
            .build()
    }

    // Tells Hilt how to make Retrofit.
    @Provides
    // Makes only one and shares it.
    @Singleton
    // Needs an OkHttpClient and a Moshi. Hilt gets them by calling the two functions above.
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        // Starts building Retrofit.
        Retrofit.Builder()
            // Sets the server address, such as https://nit3213apinew.onrender.com/.
            .baseUrl(BuildConfig.BASE_URL)
            // Uses our shared client, so the timeouts and logging apply.
            .client(okHttpClient)
            // Uses Moshi to turn JSON into Kotlin objects and back.
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            // Finishes building it.
            .build()

    // Tells Hilt how to make the NitApiService.
    @Provides
    // Makes only one and shares it with both repositories.
    @Singleton
    // Needs Retrofit, which Hilt gets from provideRetrofit() above.
    fun provideNitApiService(retrofit: Retrofit): NitApiService =
        // Asks Retrofit to write the real code behind our NitApiService interface.
        retrofit.create(NitApiService::class.java)
}
