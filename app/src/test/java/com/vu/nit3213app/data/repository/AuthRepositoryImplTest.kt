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
 */
class AuthRepositoryImplTest {

    private val apiService: NitApiService = mockk()
    private val repository = AuthRepositoryImpl(apiService, campusLocation = CAMPUS)

    @Test
    fun `successful login returns the keypass`() = runTest {
        coEvery { apiService.login(any(), any()) } returns LoginResponse(keypass = "animals")

        val result = repository.login(username = "Adam", password = "s12345678")

        assertEquals(DataResult.Success("animals"), result)
    }

    @Test
    fun `login posts the credentials to the campus endpoint`() = runTest {
        coEvery { apiService.login(any(), any()) } returns LoginResponse(keypass = "animals")

        repository.login(username = "Adam", password = "s12345678")

        coVerify {
            apiService.login(
                location = CAMPUS,
                request = LoginRequest(username = "Adam", password = "s12345678")
            )
        }
    }

    @Test
    fun `401 response means invalid credentials`() = runTest {
        coEvery { apiService.login(any(), any()) } throws httpException(401)

        val result = repository.login(username = "Adam", password = "s00000000")

        assertEquals(DataResult.Error(DataError.INVALID_CREDENTIALS), result)
    }

    @Test
    fun `400 response also means invalid credentials`() = runTest {
        coEvery { apiService.login(any(), any()) } throws httpException(400)

        val result = repository.login(username = "Adam", password = "s00000000")

        assertEquals(DataResult.Error(DataError.INVALID_CREDENTIALS), result)
    }

    @Test
    fun `500 response means a server error`() = runTest {
        coEvery { apiService.login(any(), any()) } throws httpException(500)

        val result = repository.login(username = "Adam", password = "s12345678")

        assertEquals(DataResult.Error(DataError.SERVER), result)
    }

    @Test
    fun `no internet means no connection`() = runTest {
        coEvery { apiService.login(any(), any()) } throws IOException("offline")

        val result = repository.login(username = "Adam", password = "s12345678")

        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
    }

    @Test
    fun `slow server means a timeout`() = runTest {
        coEvery { apiService.login(any(), any()) } throws SocketTimeoutException("timeout")

        val result = repository.login(username = "Adam", password = "s12345678")

        assertEquals(DataResult.Error(DataError.TIMEOUT), result)
    }

    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<LoginResponse>(code, "".toResponseBody()))

    private companion object {
        const val CAMPUS = "footscray"
    }
}
