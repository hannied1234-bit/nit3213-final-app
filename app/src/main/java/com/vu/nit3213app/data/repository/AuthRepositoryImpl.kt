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
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: NitApiService,
    @CampusLocation private val campusLocation: String
) : AuthRepository {

    override suspend fun login(username: String, password: String): DataResult<String> =
        safeApiCall(mapHttpError = ::mapLoginHttpError) {
            val request = LoginRequest(username = username, password = password)
            apiService.login(location = campusLocation, request = request).keypass
        }
}

/**
 * For the login endpoint, any 4xx reply (such as 400, 401 or 404) means the first
 * name and student ID were not accepted, so the user is told to check them.
 */
private fun mapLoginHttpError(code: Int): DataError = when (code) {
    in 400..499 -> DataError.INVALID_CREDENTIALS
    in 500..599 -> DataError.SERVER
    else -> DataError.UNKNOWN
}
