package com.vu.nit3213app.data.remote

import com.vu.nit3213app.data.remote.dto.DashboardResponse
import com.vu.nit3213app.data.remote.dto.LoginRequest
import com.vu.nit3213app.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit description of the vu-nit3213-api endpoints.
 *
 * Retrofit reads these annotations and generates the code that builds each HTTP
 * request. The functions are "suspend" so they run inside a coroutine without
 * blocking the main thread. Paths have no leading slash so they are added to the
 * end of the base URL (https://nit3213apinew.onrender.com/).
 *
 * There's no class that implements this interface anywhere in the project.
 * NetworkModule calls retrofit.create(), which builds an implementation at
 * runtime. Calling login() then sends the request, waits for the reply and uses
 * Moshi to turn the JSON into the return type.
 *
 * A suspend function here returns the parsed body directly. If the server
 * replies with a non-2xx status, Retrofit throws an HttpException, and
 * safeApiCall turns that into a DataError.
 */
interface NitApiService {

    /**
     * POST /{location}/auth, for example POST /footscray/auth.
     *
     * The campus is a path parameter rather than being typed into the URL, so the
     * same code works for the Sydney (/sydney/auth) and BR (/br/auth) classes too.
     * Only CAMPUS_LOCATION in app/build.gradle.kts needs to change.
     *
     * @Body tells Retrofit to send [request] as the JSON body, using the Moshi
     * converter added in NetworkModule.
     */
    @POST("{location}/auth")
    suspend fun login(
        @Path("location") location: String,
        @Body request: LoginRequest
    ): LoginResponse

    /**
     * GET /dashboard/{keypass}
     *
     * The keypass from login goes straight into the URL path. Retrofit
     * URL-encodes it, so a keypass with a space or slash wouldn't break the path.
     */
    @GET("dashboard/{keypass}")
    suspend fun getDashboard(
        @Path("keypass") keypass: String
    ): DashboardResponse
}
