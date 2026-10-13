// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.repository

// NitApiService: the API interface, which we replace with a mock.
import com.vu.nit3213app.data.remote.NitApiService
// LoginRequest: the login JSON body, used to check what was sent.
import com.vu.nit3213app.data.remote.dto.LoginRequest
// LoginResponse: the login reply, used as the mock's answer.
import com.vu.nit3213app.data.remote.dto.LoginResponse
// DataError: our list of error kinds.
import com.vu.nit3213app.domain.model.DataError
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// coEvery: tells a mock what to do when a suspend function is called.
import io.mockk.coEvery
// coVerify: checks a suspend function on a mock was called.
import io.mockk.coVerify
// mockk: creates a fake object for testing.
import io.mockk.mockk
// runTest: runs a test inside a coroutine, so suspend functions can be called.
import kotlinx.coroutines.test.runTest
// toResponseBody: turns text into an HTTP response body.
import okhttp3.ResponseBody.Companion.toResponseBody
// assertEquals: fails the test if two values aren't equal.
import org.junit.Assert.assertEquals
// Test: marks a function as a test for JUnit to run.
import org.junit.Test
// HttpException: the error Retrofit throws for codes like 401.
import retrofit2.HttpException
// Response: used to build a fake error reply.
import retrofit2.Response
// IOException: the error thrown when there's no internet.
import java.io.IOException
// SocketTimeoutException: the error thrown when the server is too slow.
import java.net.SocketTimeoutException

/**
 * Unit tests for [AuthRepositoryImpl]. The Retrofit service is mocked, so these
 * tests check our own logic (which URL and body we send, and how each kind of
 * failure is reported) without touching the network.
 */
// Creates the test class.
class AuthRepositoryImplTest {

    // A fake API service. It's strict: calling something not set up with coEvery throws.
    private val apiService: NitApiService = mockk()
    // The real repository, given the fake API and the Footscray campus.
    private val repository = AuthRepositoryImpl(apiService, campusLocation = CAMPUS)

    // Marks this function as a test.
    @Test
    // Checks a successful login gives back the keypass. runTest lets us call suspend functions.
    fun `successful login returns the keypass`() = runTest {
        // Tells the fake API to reply with keypass "animals" whatever it's given.
        coEvery { apiService.login(any(), any()) } returns LoginResponse(keypass = "animals")

        // Logs in with a student ID and first name.
        val result = repository.login(username = "12345678", password = "Adam")

        // Checks the result is Success with the keypass.
        assertEquals(DataResult.Success("animals"), result)
    }

    // Marks this function as a test.
    @Test
    // Checks the right campus and body are sent.
    fun `login posts the credentials to the campus endpoint`() = runTest {
        // Tells the fake API to reply successfully.
        coEvery { apiService.login(any(), any()) } returns LoginResponse(keypass = "animals")

        // Logs in.
        repository.login(username = "12345678", password = "Adam")

        // Checks the fake API was called with exactly these values.
        coVerify {
            // The call we expect.
            apiService.login(
                // The campus, "footscray".
                location = CAMPUS,
                // The body, with the ID as username and the name as password.
                request = LoginRequest(username = "12345678", password = "Adam")
            )
        }
    }

    // Marks this function as a test.
    @Test
    // Checks a 401 reply is reported as wrong details. "adam" in lowercase is the realistic mistake.
    fun `401 response means invalid credentials`() = runTest {
        // Tells the fake API to throw a 401 error.
        coEvery { apiService.login(any(), any()) } throws httpException(401)

        // Logs in with the wrong capitalisation.
        val result = repository.login(username = "12345678", password = "adam")

        // Checks the result is the "wrong details" error.
        assertEquals(DataResult.Error(DataError.INVALID_CREDENTIALS), result)
    }

    // Marks this function as a test.
    @Test
    // Checks a 400 reply is also reported as wrong details.
    fun `400 response also means invalid credentials`() = runTest {
        // Tells the fake API to throw a 400 error.
        coEvery { apiService.login(any(), any()) } throws httpException(400)

        // Logs in.
        val result = repository.login(username = "12345678", password = "adam")

        // Checks it's the "wrong details" error too.
        assertEquals(DataResult.Error(DataError.INVALID_CREDENTIALS), result)
    }

    // Marks this function as a test.
    @Test
    // Checks a 500 reply is reported as a server problem.
    fun `500 response means a server error`() = runTest {
        // Tells the fake API to throw a 500 error.
        coEvery { apiService.login(any(), any()) } throws httpException(500)

        // Logs in.
        val result = repository.login(username = "12345678", password = "Adam")

        // Checks it's the server error.
        assertEquals(DataResult.Error(DataError.SERVER), result)
    }

    // Marks this function as a test.
    @Test
    // Checks no internet is reported as no connection.
    fun `no internet means no connection`() = runTest {
        // Tells the fake API to throw the "no internet" error.
        coEvery { apiService.login(any(), any()) } throws IOException("offline")

        // Logs in.
        val result = repository.login(username = "12345678", password = "Adam")

        // Checks it's the no connection error.
        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
    }

    // Marks this function as a test.
    @Test
    // Checks a slow server is reported as a timeout, not as no internet.
    fun `slow server means a timeout`() = runTest {
        // Tells the fake API to throw the timeout error.
        coEvery { apiService.login(any(), any()) } throws SocketTimeoutException("timeout")

        // Logs in.
        val result = repository.login(username = "12345678", password = "Adam")

        // Checks it's the timeout error.
        // Why this matters: it proves the catch blocks in safeApiCall are in the right order.
        assertEquals(DataResult.Error(DataError.TIMEOUT), result)
    }

    // A helper that builds the error Retrofit throws for a given code, such as 401.
    private fun httpException(code: Int): HttpException =
        // Wraps a fake error reply with that code and an empty body.
        HttpException(Response.error<LoginResponse>(code, "".toResponseBody()))

    // Holds fixed values for this test class.
    private companion object {
        // The campus used in every test.
        const val CAMPUS = "footscray"
    }
}
