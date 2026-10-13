// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.repository

// EntityMapper: the real mapper, used instead of a mock.
import com.vu.nit3213app.data.mapper.EntityMapper
// NitApiService: the API interface, which we replace with a mock.
import com.vu.nit3213app.data.remote.NitApiService
// DashboardResponse: the dashboard reply, used as the mock's answer.
import com.vu.nit3213app.data.remote.dto.DashboardResponse
// DashboardData: the tidy result we expect back.
import com.vu.nit3213app.domain.model.DashboardData
// DataError: our list of error kinds.
import com.vu.nit3213app.domain.model.DataError
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// Entity: one tidy item, used to build the expected result.
import com.vu.nit3213app.domain.model.Entity
// EntityProperty: one label and value pair.
import com.vu.nit3213app.domain.model.EntityProperty
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
// HttpException: the error Retrofit throws for codes like 404.
import retrofit2.HttpException
// Response: used to build a fake error reply.
import retrofit2.Response
// IOException: the error thrown when there's no internet.
import java.io.IOException

/**
 * Unit tests for [DashboardRepositoryImpl]. The API is mocked, while the real
 * EntityMapper is used because it is plain Kotlin with no side effects. That
 * proves a raw JSON-shaped reply comes out as the exact Entities the screen shows.
 */
// Creates the test class.
class DashboardRepositoryImplTest {

    // A fake API service.
    private val apiService: NitApiService = mockk()
    // The real repository, given the fake API and the real mapper.
    private val repository = DashboardRepositoryImpl(apiService, EntityMapper())

    // Marks this function as a test.
    @Test
    // Checks a raw reply is turned into tidy dashboard data.
    fun `maps the API response into dashboard data`() = runTest {
        // Given: a reply shaped like real Moshi output.
        val response = DashboardResponse(
            // One raw item.
            entities = listOf(
                // Its fields.
                mapOf(
                    // A camelCase field.
                    "artistName" to "Pablo Picasso",
                    // A number, as a Double like Moshi gives.
                    "year" to 1937.0,
                    // The description.
                    "description" to "A famous painting."
                )
            ),
            // The total.
            entityTotal = 1
        )
        // Tells the fake API to return that reply for keypass "art".
        coEvery { apiService.getDashboard("art") } returns response

        // When: fetch the dashboard.
        val result = repository.getDashboard("art")

        // Then: build what we expect to get back.
        val expected = DashboardData(
            // One tidy Entity.
            entities = listOf(
                // The Entity itself.
                Entity(
                    // Its fields, without the description.
                    properties = listOf(
                        // Readable label.
                        EntityProperty(label = "Artist name", value = "Pablo Picasso"),
                        // The year has lost its ".0".
                        EntityProperty(label = "Year", value = "1937")
                    ),
                    // The description, kept separately.
                    description = "A famous painting."
                )
            ),
            // The total.
            total = 1
        )
        // Checks the result matches exactly.
        assertEquals(DataResult.Success(expected), result)
        // Checks the API was called exactly once.
        coVerify(exactly = 1) { apiService.getDashboard("art") }
    }

    // Marks this function as a test.
    @Test
    // Checks a 404 reply means the keypass wasn't found.
    fun `404 response means the keypass was not found`() = runTest {
        // Tells the fake API to throw a 404 error...
        coEvery { apiService.getDashboard(any()) } throws
            // ...built from a fake error reply.
            HttpException(Response.error<DashboardResponse>(404, "".toResponseBody()))

        // Fetches with an unknown keypass.
        val result = repository.getDashboard("unknown")

        // Checks it's the not found error.
        assertEquals(DataResult.Error(DataError.NOT_FOUND), result)
    }

    // Marks this function as a test.
    @Test
    // Checks no internet is reported as no connection.
    fun `no internet means no connection`() = runTest {
        // Tells the fake API to throw the "no internet" error.
        coEvery { apiService.getDashboard(any()) } throws IOException("offline")

        // Fetches.
        val result = repository.getDashboard("art")

        // Checks it's the no connection error.
        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
    }

    // Marks this function as a test.
    @Test
    // Checks an unexpected error gives a result instead of crashing.
    fun `unexpected exception is reported as unknown instead of crashing`() = runTest {
        // Tells the fake API to throw an error nobody planned for.
        coEvery { apiService.getDashboard(any()) } throws IllegalStateException("bad JSON shape")

        // Fetches.
        val result = repository.getDashboard("art")

        // Checks it's the unknown error.
        assertEquals(DataResult.Error(DataError.UNKNOWN), result)
    }
}
