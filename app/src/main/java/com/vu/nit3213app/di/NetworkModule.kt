package com.vu.nit3213app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vu.nit3213app.BuildConfig
import com.vu.nit3213app.data.remote.NitApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module that tells Hilt how to build the networking objects.
 *
 * These come from third-party libraries (we cannot add @Inject to their
 * constructors), so each one gets a @Provides function instead. Installing the
 * module in SingletonComponent and marking each function @Singleton means one
 * shared instance is created for the whole app, which is important for OkHttp
 * because it reuses connections between requests.
 *
 * The functions form a chain. provideRetrofit() asks for an OkHttpClient and a
 * Moshi in its parameters, and Hilt answers by calling the two functions above
 * it. provideNitApiService() asks for the Retrofit. No code here calls another
 * @Provides function directly. Hilt works out the order from the parameter
 * types at compile time.
 *
 * It's an object rather than a class because the functions don't need any
 * instance state, and Hilt can call them without creating the module first.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * The API is hosted on Render's free tier, which puts the server to sleep
     * when it is idle. The first request can take up to a minute to wake it up,
     * so the timeouts are longer than OkHttp's 10 second default.
     */
    private const val TIMEOUT_SECONDS = 60L

    // Not a @Singleton because it's a cheap String constant. Hilt calls this
    // each time a @CampusLocation String is needed, and the value never changes.
    @Provides
    @CampusLocation
    fun provideCampusLocation(): String = BuildConfig.CAMPUS_LOCATION

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        // Lets Moshi understand Kotlin data classes, including default values.
        // It also respects Kotlin null safety, so a missing non-null field
        // fails right away with a clear error instead of sneaking in a null.
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Prints every request and response to Logcat in debug builds, which is
        // handy for seeing the real JSON your topic returns. Note this includes
        // the login body, so it is switched off in release builds.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // All three timeouts are raised. connectTimeout covers opening the
        // socket, readTimeout covers waiting for the reply, and writeTimeout
        // covers sending the body. A sleeping server can be slow at any of them.
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            // Retrofit requires the base URL to end with "/". The endpoint paths
            // in NitApiService have no leading slash, so the two join cleanly.
            .baseUrl(BuildConfig.BASE_URL)
            // Passing the shared client in means Retrofit uses the timeouts and
            // logging set up above instead of building a default client.
            .client(okHttpClient)
            // Converts request bodies to JSON and JSON replies back to the DTOs.
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    // retrofit.create() is fairly expensive because it reads every annotation
    // on the interface, so the result is kept as a singleton and shared by
    // both repositories.
    @Provides
    @Singleton
    fun provideNitApiService(retrofit: Retrofit): NitApiService =
        retrofit.create(NitApiService::class.java)
}
