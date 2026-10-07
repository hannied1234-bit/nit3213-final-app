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
 * end of the base URL (https://nit3213api.onrender.com/).
 */
interface NitApiService {

    /**
     * POST /{location}/auth, for example POST /footscray/auth.
     *
     * The campus is a path parameter rather than being typed into the URL, so the
     * same code would work for the Sydney or ORT classes too.
     */
    @POST("{location}/auth")
    suspend fun login(
        @Path("location") location: String,
        @Body request: LoginRequest
    ): LoginResponse

    /** GET /dashboard/{keypass} */
    @GET("dashboard/{keypass}")
    suspend fun getDashboard(
        @Path("keypass") keypass: String
    ): DashboardResponse
}
