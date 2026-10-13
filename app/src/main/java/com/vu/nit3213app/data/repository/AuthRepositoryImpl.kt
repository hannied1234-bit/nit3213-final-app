// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.repository

// NitApiService: the Retrofit description of the server's endpoints.
import com.vu.nit3213app.data.remote.NitApiService
// LoginRequest: the shape of the JSON we send to log in.
import com.vu.nit3213app.data.remote.dto.LoginRequest
// safeApiCall: our helper that turns network errors into DataResult.Error.
import com.vu.nit3213app.data.remote.safeApiCall
// CampusLocation: the Hilt name tag for the campus String.
import com.vu.nit3213app.di.CampusLocation
// DataError: our list of error kinds.
import com.vu.nit3213app.domain.model.DataError
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// AuthRepository: the interface this class fills in.
import com.vu.nit3213app.domain.repository.AuthRepository
// Inject: the label that tells Hilt it can build this class.
import javax.inject.Inject

/**
 * Network implementation of [AuthRepository].
 *
 * Constructor injection: Hilt sees @Inject and passes in the API service and the
 * campus location, so this class never creates its own dependencies. In a unit
 * test we can pass in a mock API service and any campus instead.
 */
// Creates the class that really does the login. "@Inject constructor" lets Hilt build it.
class AuthRepositoryImpl @Inject constructor(
    // Asks Hilt for the API service, which sends the actual request.
    private val apiService: NitApiService,
    // Asks Hilt for the campus String, such as "footscray".
    // Why the tag: it tells Hilt exactly which String to give, in case there are several.
    @CampusLocation private val campusLocation: String
// ": AuthRepository" means this class fills in the AuthRepository interface.
) : AuthRepository {

    // Fills in the interface's login function. Returns the keypass, or an error.
    override suspend fun login(username: String, password: String): DataResult<String> =
        // Runs the request safely, using the login-specific error rule below.
        safeApiCall(mapHttpError = ::mapLoginHttpError) {
            // Builds the JSON body from the student ID and first name.
            val request = LoginRequest(username = username, password = password)
            // Sends the request to the campus endpoint, then takes only the keypass from the reply.
            apiService.login(location = campusLocation, request = request).keypass
        }
}

/**
 * For the login endpoint, any 4xx reply (such as 400, 401 or 404) means the
 * student ID and first name were not accepted, so the user is told to check them.
 * The default rule would call a 404 "not found", which would confuse the user.
 */
// Takes an HTTP error code from the login endpoint and returns a DataError.
private fun mapLoginHttpError(code: Int): DataError = when (code) {
    // Any code from 400 to 499 means the details were wrong.
    in 400..499 -> DataError.INVALID_CREDENTIALS
    // Any code from 500 to 599 means the server had a problem.
    in 500..599 -> DataError.SERVER
    // Anything else is unknown.
    else -> DataError.UNKNOWN
}
