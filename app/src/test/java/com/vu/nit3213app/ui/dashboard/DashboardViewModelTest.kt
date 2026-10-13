// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.dashboard

// SavedStateHandle: holds the values passed to a screen, such as the keypass.
import androidx.lifecycle.SavedStateHandle
// R: the generated list of IDs for strings, used to check error messages.
import com.vu.nit3213app.R
// DashboardData: the list of entities plus the total.
import com.vu.nit3213app.domain.model.DashboardData
// DataError: our list of error kinds.
import com.vu.nit3213app.domain.model.DataError
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// Entity: one tidy item.
import com.vu.nit3213app.domain.model.Entity
// EntityProperty: one label and value pair.
import com.vu.nit3213app.domain.model.EntityProperty
// DashboardRepository: the interface we replace with a mock.
import com.vu.nit3213app.domain.repository.DashboardRepository
// MainDispatcherRule: our rule that makes viewModelScope work in tests.
import com.vu.nit3213app.util.MainDispatcherRule
// coEvery: tells a mock what to do when a suspend function is called.
import io.mockk.coEvery
// coVerify: checks a suspend function on a mock was called.
import io.mockk.coVerify
// mockk: creates a fake object for testing.
import io.mockk.mockk
// CompletableDeferred: a value we can hold back and release later.
import kotlinx.coroutines.CompletableDeferred
// ExperimentalCoroutinesApi: marks test tools that are still officially "experimental".
import kotlinx.coroutines.ExperimentalCoroutinesApi
// advanceUntilIdle: lets waiting coroutines finish.
import kotlinx.coroutines.test.advanceUntilIdle
// runTest: runs a test inside a coroutine.
import kotlinx.coroutines.test.runTest
// assertEquals: fails the test if two values aren't equal.
import org.junit.Assert.assertEquals
// Rule: marks a JUnit rule.
import org.junit.Rule
// Test: marks a function as a test for JUnit to run.
import org.junit.Test

/**
 * Unit tests for [DashboardViewModel].
 *
 * The ViewModel loads data in its init block, so the mock repository is set up
 * before each ViewModel is created. The keypass is supplied through a
 * SavedStateHandle, exactly as Safe Args would supply it in the running app.
 */
// Allows the experimental test tools to be used here.
@OptIn(ExperimentalCoroutinesApi::class)
// Creates the test class.
class DashboardViewModelTest {

    // Registers the rule. "@get:" puts the label where JUnit 4 looks for it.
    @get:Rule
    // Swaps in a fake Main thread so viewModelScope works in tests.
    val mainDispatcherRule = MainDispatcherRule()

    // A fake repository.
    private val dashboardRepository: DashboardRepository = mockk()

    // A sample Entity, reused by several tests.
    private val sampleEntity = Entity(
        // Its fields.
        properties = listOf(
            // First field.
            EntityProperty(label = "Artist name", value = "Pablo Picasso"),
            // Second field.
            EntityProperty(label = "Year", value = "1937")
        ),
        // Its description.
        description = "A famous painting."
    )

    // A helper that creates the ViewModel with a keypass. Passing null leaves the keypass out.
    private fun createViewModel(keypass: String? = KEYPASS): DashboardViewModel {
        // Builds the screen's saved values: empty if no keypass, otherwise the keypass.
        val arguments = if (keypass == null) emptyMap() else mapOf(KEYPASS_KEY to keypass)
        // Creates the ViewModel with the fake repository and those values.
        return DashboardViewModel(dashboardRepository, SavedStateHandle(arguments))
    }

    // Marks this function as a test.
    @Test
    // Checks the list loads straight away using the keypass.
    fun `loads entities on start using the keypass from navigation`() {
        // Given: the fake repository returns one item for keypass "art".
        coEvery { dashboardRepository.getDashboard(KEYPASS) } returns
            // A Success with the sample item.
            DataResult.Success(DashboardData(entities = listOf(sampleEntity), total = 1))

        // When: create the ViewModel. Its init block starts loading.
        val viewModel = createViewModel()

        // Then: check the state.
        assertEquals(
            // The state we expect.
            DashboardUiState.Success(entities = listOf(sampleEntity), total = 1),
            // The state we got.
            viewModel.uiState.value
        )
        // Checks the repository was called exactly once with the keypass.
        coVerify(exactly = 1) { dashboardRepository.getDashboard(KEYPASS) }
    }

    // Marks this function as a test.
    @Test
    // Checks an empty list still counts as a success.
    fun `empty list from the API is still a success`() {
        // Tells the fake repository to return an empty list.
        coEvery { dashboardRepository.getDashboard(any()) } returns
            // A Success with no items.
            DataResult.Success(DashboardData(entities = emptyList(), total = 0))

        // Creates the ViewModel.
        val viewModel = createViewModel()

        // Checks the state is Success with an empty list.
        assertEquals(DashboardUiState.Success(entities = emptyList(), total = 0), viewModel.uiState.value)
    }

    // Marks this function as a test.
    @Test
    // Checks an error from the repository becomes an error message.
    fun `API error is shown as an error message`() {
        // Tells the fake repository to return "not found".
        coEvery { dashboardRepository.getDashboard(any()) } returns
            // An Error.
            DataResult.Error(DataError.NOT_FOUND)

        // Creates the ViewModel.
        val viewModel = createViewModel()

        // Checks the state is Error with the "not found" message.
        assertEquals(DashboardUiState.Error(R.string.error_not_found), viewModel.uiState.value)
    }

    // Marks this function as a test.
    @Test
    // Checks a missing keypass shows an error without calling the server.
    fun `missing keypass shows an error without calling the API`() {
        // Creates the ViewModel with no keypass. No coEvery: the strict mock would throw if called.
        val viewModel = createViewModel(keypass = null)

        // Checks the state is the "session expired" error.
        assertEquals(DashboardUiState.Error(R.string.error_missing_keypass), viewModel.uiState.value)
        // Checks the repository was never called.
        coVerify(exactly = 0) { dashboardRepository.getDashboard(any()) }
    }

    // Marks this function as a test.
    @Test
    // Checks "Try again" loads the data after an error.
    fun `try again after an error loads the data`() {
        // Given: the first call fails and the second works. returnsMany hands them out in order.
        coEvery { dashboardRepository.getDashboard(any()) } returnsMany listOf(
            // First call: no internet.
            DataResult.Error(DataError.NO_CONNECTION),
            // Second call: success.
            DataResult.Success(DashboardData(entities = listOf(sampleEntity), total = 1))
        )
        // Creates the ViewModel, which makes the first (failing) call.
        val viewModel = createViewModel()
        // Checks it's showing the no connection error.
        assertEquals(DashboardUiState.Error(R.string.error_no_connection), viewModel.uiState.value)

        // When: the user taps "Try again".
        viewModel.loadEntities()

        // Then: check the state.
        assertEquals(
            // The state we expect.
            DashboardUiState.Success(entities = listOf(sampleEntity), total = 1),
            // The state we got.
            viewModel.uiState.value
        )
        // Checks the repository was called twice.
        coVerify(exactly = 2) { dashboardRepository.getDashboard(KEYPASS) }
    }

    // Marks this function as a test.
    @Test
    // Checks the state is Loading while waiting for the server.
    fun `state is loading until the API responds`() = runTest {
        // A result we hold back until we're ready.
        val pendingResult = CompletableDeferred<DataResult<DashboardData>>()
        // Tells the fake repository to wait for that held-back result.
        coEvery { dashboardRepository.getDashboard(any()) } coAnswers { pendingResult.await() }

        // Creates the ViewModel, which starts waiting.
        val viewModel = createViewModel()
        // Checks it's Loading while it waits.
        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)

        // Releases the result.
        pendingResult.complete(DataResult.Success(DashboardData(entities = listOf(sampleEntity), total = 1)))
        // Lets the waiting coroutine finish.
        advanceUntilIdle()

        // Checks the state is now Success.
        assertEquals(
            // The state we expect.
            DashboardUiState.Success(entities = listOf(sampleEntity), total = 1),
            // The state we got.
            viewModel.uiState.value
        )
    }

    // Holds fixed values for this test class.
    private companion object {
        // The keypass used in the tests.
        const val KEYPASS = "art"
        // The name the keypass is saved under. Uses the ViewModel's own constant so they always match.
        const val KEYPASS_KEY = DashboardViewModel.KEYPASS_ARGUMENT
    }
}
