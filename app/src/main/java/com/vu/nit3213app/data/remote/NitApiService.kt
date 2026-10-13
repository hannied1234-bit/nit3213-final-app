// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.remote

// DashboardResponse: the shape of the JSON the dashboard endpoint sends back.
import com.vu.nit3213app.data.remote.dto.DashboardResponse
// LoginRequest: the shape of the JSON we send to log in.
import com.vu.nit3213app.data.remote.dto.LoginRequest
// LoginResponse: the shape of the JSON the login endpoint sends back.
import com.vu.nit3213app.data.remote.dto.LoginResponse
// Body: marks which value is sent as the request's JSON body.
import retrofit2.http.Body
// GET: marks a request that fetches data.
import retrofit2.http.GET
// POST: marks a request that sends data.
import retrofit2.http.POST
// Path: marks a value that's slotted into the URL.
import retrofit2.http.Path

/**
 * Retrofit description of the vu-nit3213-api endpoints.
 *
 * There's no class that implements this interface anywhere in the project.
 * NetworkModule calls retrofit.create(), which builds one while the app runs.
 * Calling login() then sends the request, waits for the reply and uses Moshi to
 * turn the JSON into the return type.
 *
 * If the server replies with an error status, Retrofit throws an HttpException,
 * and safeApiCall turns that into a DataError.
 */
// Describes the server's endpoints. Retrofit reads this and writes the real code for us.
interface NitApiService {

    /**
     * POST /{location}/auth, for example POST /footscray/auth.
     * The same code works for /sydney/auth and /br/auth. Only CAMPUS_LOCATION
     * in app/build.gradle.kts needs to change.
     */
    // Sends a POST request to "{location}/auth". The {location} part is filled in below.
    // Why no leading slash: so it's added to the end of the base URL.
    @POST("{location}/auth")
    // The login request. "suspend" means it waits for the server without freezing the app.
    suspend fun login(
        // This value replaces {location} in the URL, such as "footscray".
        @Path("location") location: String,
        // This object is turned into JSON and sent as the request body.
        @Body request: LoginRequest
    // The reply is turned from JSON into a LoginResponse.
    ): LoginResponse

    /**
     * GET /dashboard/{keypass}
     */
    // Sends a GET request to "dashboard/{keypass}".
    @GET("dashboard/{keypass}")
    // The dashboard request. Also "suspend", so it waits without freezing the app.
    suspend fun getDashboard(
        // This value replaces {keypass} in the URL, such as "animals".
        @Path("keypass") keypass: String
    // The reply is turned from JSON into a DashboardResponse.
    ): DashboardResponse
}
