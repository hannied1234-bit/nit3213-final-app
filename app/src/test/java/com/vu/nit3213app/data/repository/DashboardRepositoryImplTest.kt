package com.vu.nit3213app.data.repository

import com.vu.nit3213app.data.mapper.EntityMapper
import com.vu.nit3213app.data.remote.NitApiService
import com.vu.nit3213app.data.remote.dto.DashboardResponse
import com.vu.nit3213app.domain.model.DashboardData
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.model.Entity
import com.vu.nit3213app.domain.model.EntityProperty
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

/**
 * Unit tests for [DashboardRepositoryImpl]. The API is mocked, while the real
 * EntityMapper is used because it is plain Kotlin with no side effects.
 *
 * Using the real mapper makes these small integration tests. They check that
 * a raw JSON-shaped response comes out as the exact Entity objects the screen
 * will show, which a mocked mapper couldn't prove.
 */
class DashboardRepositoryImplTest {

    private val apiService: NitApiService = mockk()
    private val repository = DashboardRepositoryImpl(apiService, EntityMapper())

    @Test
    fun `maps the API response into dashboard data`() = runTest {
        // Given a response shaped like real Moshi output: the year is a Double
        // and the description sits among the other fields
        val response = DashboardResponse(
            entities = listOf(
                mapOf(
                    "artistName" to "Pablo Picasso",
                    "year" to 1937.0,
                    "description" to "A famous painting."
                )
            ),
            entityTotal = 1
        )
        coEvery { apiService.getDashboard("art") } returns response

        // When
        val result = repository.getDashboard("art")

        // Then the labels are readable, the year has no ".0" and the
        // description has been moved out of the properties
        val expected = DashboardData(
            entities = listOf(
                Entity(
                    properties = listOf(
                        EntityProperty(label = "Artist name", value = "Pablo Picasso"),
                        EntityProperty(label = "Year", value = "1937")
                    ),
                    description = "A famous painting."
                )
            ),
            total = 1
        )
        assertEquals(DataResult.Success(expected), result)
        // exactly = 1 makes sure the repository doesn't retry or call twice.
        coVerify(exactly = 1) { apiService.getDashboard("art") }
    }

    // The dashboard endpoint returns 404 for a keypass it doesn't know.
    @Test
    fun `404 response means the keypass was not found`() = runTest {
        coEvery { apiService.getDashboard(any()) } throws
            HttpException(Response.error<DashboardResponse>(404, "".toResponseBody()))

        val result = repository.getDashboard("unknown")

        assertEquals(DataResult.Error(DataError.NOT_FOUND), result)
    }

    @Test
    fun `no internet means no connection`() = runTest {
        coEvery { apiService.getDashboard(any()) } throws IOException("offline")

        val result = repository.getDashboard("art")

        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
    }

    // Covers the final catch-all in safeApiCall. Any exception that isn't a
    // network one must still come back as a result instead of crashing the app.
    @Test
    fun `unexpected exception is reported as unknown instead of crashing`() = runTest {
        coEvery { apiService.getDashboard(any()) } throws IllegalStateException("bad JSON shape")

        val result = repository.getDashboard("art")

        assertEquals(DataResult.Error(DataError.UNKNOWN), result)
    }
}
