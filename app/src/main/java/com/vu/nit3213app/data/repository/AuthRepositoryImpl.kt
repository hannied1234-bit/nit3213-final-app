package com.vu.nit3213app.data.repository

import com.vu.nit3213app.data.remote.NitApiService
import com.vu.nit3213app.data.remote.dto.LoginRequest
import com.vu.nit3213app.data.remote.safeApiCall
import com.vu.nit3213app.di.CampusLocation
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Network implementation of [AuthRepository].
 *
 * Constructor injection: Hilt sees @Inject and passes in the API service and the
 * campus location, so this class never creates its own dependencies. In a unit
 * test we can pass in a mock API service and any campus instead.
 *
 * The class turns the domain-level request ("log in with these two strings")
 * into the API's shape (a LoginRequest posted to the campus URL). It then
 * reduces the reply to just the keypass, since that's all the app needs.
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: NitApiService,
    // The qualifier tells Hilt to use the String from provideCampusLocation().
    // Without it, Hilt wouldn't know which String to inject here.
    @CampusLocation private val campusLocation: String
) : AuthRepository {

    // The expression body passes the login-specific error rule to
    // safeApiCall. Everything inside the braces runs inside its try block, so
    // any exception thrown by the request becomes a DataResult.Error.
    override suspend fun login(username: String, password: String): DataResult<String> =
        safeApiCall(mapHttpError = ::mapLoginHttpError) {
            val request = LoginRequest(username = username, password = password)
            // .keypass pulls the one useful field out of the response, so
            // callers get a plain String and never see the DTO.
            apiService.login(location = campusLocation, request = request).keypass
        }
}

/**
 * For the login endpoint, any 4xx reply (such as 400, 401 or 404) means the first
 * name and student ID were not accepted, so the user is told to check them.
 *
 * The default rule in safeApiCall would call a 404 "not found", which would
 * confuse someone who just mistyped their student ID. This rule replaces it for
 * login only. It's private to the file because nothing else should use it.
 */
private fun mapLoginHttpError(code: Int): DataError = when (code) {
    in 400..499 -> DataError.INVALID_CREDENTIALS
    in 500..599 -> DataError.SERVER
    else -> DataError.UNKNOWN
}
