package com.vu.nit3213app.data.repository

import com.vu.nit3213app.data.remote.NitApiService
import com.vu.nit3213app.data.remote.dto.LoginRequest
import com.vu.nit3213app.data.remote.dto.LoginResponse
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for [AuthRepositoryImpl]. The Retrofit service is mocked, so these
 * tests check our own logic (which URL and body we send, and how each kind of
 * failure is reported) without touching the network.
 *
 * Each failure test makes the mock throw the same exception Retrofit or OkHttp
 * would throw in real life. That covers the repository and safeApiCall together,
 * which is the code path the app actually runs.
 *
 * runTest is needed because the repository's functions suspend. It runs the
 * test body in a coroutine and skips any delays, so the tests finish instantly.
 */
class AuthRepositoryImplTest {

    // mockk() creates a strict mock. Calling a function that hasn't been set
    // up with coEvery throws, so a test can't pass by accident on an
    // unexpected call.
    private val apiService: NitApiService = mockk()
    private val repository = AuthRepositoryImpl(apiService, campusLocation = CAMPUS)

    @Test
    fun `successful login returns the keypass`() = runTest {
        // coEvery is the coroutine version of every. It's needed because
        // login() is a suspend function.
        coEvery { apiService.login(any(), any()) } returns LoginResponse(keypass = "animals")

        val result = repository.login(username = "12345678", password = "Adam")

        assertEquals(DataResult.Success("animals"), result)
    }

    // Checks the outgoing request instead of the result. A typo in the campus
    // or a swapped username and password would fail here.
    @Test
    fun `login posts the credentials to the campus endpoint`() = runTest {
        coEvery { apiService.login(any(), any()) } returns LoginResponse(keypass = "animals")

        repository.login(username = "12345678", password = "Adam")

        // LoginRequest is a data class, so MockK compares it by value.
        coVerify {
            apiService.login(
                location = CAMPUS,
                request = LoginRequest(username = "12345678", password = "Adam")
            )
        }
    }

    // "adam" in lowercase stands for the most likely real mistake. The first
    // name is case sensitive, so the server rejects it.
    @Test
    fun `401 response means invalid credentials`() = runTest {
        coEvery { apiService.login(any(), any()) } throws httpException(401)

        val result = repository.login(username = "12345678", password = "adam")

        assertEquals(DataResult.Error(DataError.INVALID_CREDENTIALS), result)
    }

    // The API returns 400 for some bad logins, so the whole 4xx range has to
    // count as wrong credentials, not just 401.
    @Test
    fun `400 response also means invalid credentials`() = runTest {
        coEvery { apiService.login(any(), any()) } throws httpException(400)

        val result = repository.login(username = "12345678", password = "adam")

        assertEquals(DataResult.Error(DataError.INVALID_CREDENTIALS), result)
    }

    @Test
    fun `500 response means a server error`() = runTest {
        coEvery { apiService.login(any(), any()) } throws httpException(500)

        val result = repository.login(username = "12345678", password = "Adam")

        assertEquals(DataResult.Error(DataError.SERVER), result)
    }

    @Test
    fun `no internet means no connection`() = runTest {
        coEvery { apiService.login(any(), any()) } throws IOException("offline")

        val result = repository.login(username = "12345678", password = "Adam")

        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
    }

    // SocketTimeoutException is an IOException. This test proves the catch
    // order in safeApiCall is right and a timeout isn't reported as offline.
    @Test
    fun `slow server means a timeout`() = runTest {
        coEvery { apiService.login(any(), any()) } throws SocketTimeoutException("timeout")

        val result = repository.login(username = "12345678", password = "Adam")

        assertEquals(DataResult.Error(DataError.TIMEOUT), result)
    }

    // Builds the same HttpException Retrofit throws for a non-2xx reply. The
    // empty body is fine because the repository only reads the status code.
    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<LoginResponse>(code, "".toResponseBody()))

    private companion object {
        const val CAMPUS = "footscray"
    }
}
