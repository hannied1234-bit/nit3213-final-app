package com.vu.nit3213app.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import com.vu.nit3213app.R
import com.vu.nit3213app.domain.model.DashboardData
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.model.Entity
import com.vu.nit3213app.domain.model.EntityProperty
import com.vu.nit3213app.domain.repository.DashboardRepository
import com.vu.nit3213app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [DashboardViewModel].
 *
 * The ViewModel loads data in its init block, so the mock repository is set up
 * before each ViewModel is created. The keypass is supplied through a
 * SavedStateHandle, exactly as Safe Args would supply it in the running app.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dashboardRepository: DashboardRepository = mockk()

    private val sampleEntity = Entity(
        properties = listOf(
            EntityProperty(label = "Artist name", value = "Pablo Picasso"),
            EntityProperty(label = "Year", value = "1937")
        ),
        description = "A famous painting."
    )

    private fun createViewModel(keypass: String? = KEYPASS): DashboardViewModel {
        val arguments = if (keypass == null) emptyMap() else mapOf(KEYPASS_KEY to keypass)
        return DashboardViewModel(dashboardRepository, SavedStateHandle(arguments))
    }

    @Test
    fun `loads entities on start using the keypass from navigation`() {
        // Given
        coEvery { dashboardRepository.getDashboard(KEYPASS) } returns
            DataResult.Success(DashboardData(entities = listOf(sampleEntity), total = 1))

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals(
            DashboardUiState.Success(entities = listOf(sampleEntity), total = 1),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { dashboardRepository.getDashboard(KEYPASS) }
    }

    @Test
    fun `empty list from the API is still a success`() {
        coEvery { dashboardRepository.getDashboard(any()) } returns
            DataResult.Success(DashboardData(entities = emptyList(), total = 0))

        val viewModel = createViewModel()

        assertEquals(DashboardUiState.Success(entities = emptyList(), total = 0), viewModel.uiState.value)
    }

    @Test
    fun `API error is shown as an error message`() {
        coEvery { dashboardRepository.getDashboard(any()) } returns
            DataResult.Error(DataError.NOT_FOUND)

        val viewModel = createViewModel()

        assertEquals(DashboardUiState.Error(R.string.error_not_found), viewModel.uiState.value)
    }

    @Test
    fun `missing keypass shows an error without calling the API`() {
        val viewModel = createViewModel(keypass = null)

        assertEquals(DashboardUiState.Error(R.string.error_missing_keypass), viewModel.uiState.value)
        coVerify(exactly = 0) { dashboardRepository.getDashboard(any()) }
    }

    @Test
    fun `try again after an error loads the data`() {
        // Given the first call fails and the second succeeds
        coEvery { dashboardRepository.getDashboard(any()) } returnsMany listOf(
            DataResult.Error(DataError.NO_CONNECTION),
            DataResult.Success(DashboardData(entities = listOf(sampleEntity), total = 1))
        )
        val viewModel = createViewModel()
        assertEquals(DashboardUiState.Error(R.string.error_no_connection), viewModel.uiState.value)

        // When the user taps "Try again"
        viewModel.loadEntities()

        // Then
        assertEquals(
            DashboardUiState.Success(entities = listOf(sampleEntity), total = 1),
            viewModel.uiState.value
        )
        coVerify(exactly = 2) { dashboardRepository.getDashboard(KEYPASS) }
    }

    @Test
    fun `state is loading until the API responds`() = runTest {
        val pendingResult = CompletableDeferred<DataResult<DashboardData>>()
        coEvery { dashboardRepository.getDashboard(any()) } coAnswers { pendingResult.await() }

        val viewModel = createViewModel()
        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)

        pendingResult.complete(DataResult.Success(DashboardData(entities = listOf(sampleEntity), total = 1)))
        advanceUntilIdle()

        assertEquals(
            DashboardUiState.Success(entities = listOf(sampleEntity), total = 1),
            viewModel.uiState.value
        )
    }

    private companion object {
        const val KEYPASS = "art"
        const val KEYPASS_KEY = DashboardViewModel.KEYPASS_ARGUMENT
    }
}
