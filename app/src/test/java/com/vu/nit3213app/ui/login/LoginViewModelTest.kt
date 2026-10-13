// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.login

// R: the generated list of IDs for strings, used to check error messages.
import com.vu.nit3213app.R
// DataError: our list of error kinds.
import com.vu.nit3213app.domain.model.DataError
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// AuthRepository: the interface we replace with a mock.
import com.vu.nit3213app.domain.repository.AuthRepository
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
// assertFalse: fails the test if the value is true.
import org.junit.Assert.assertFalse
// assertNull: fails the test if the value isn't null.
import org.junit.Assert.assertNull
// assertTrue: fails the test if the value is false.
import org.junit.Assert.assertTrue
// Before: marks a function to run before each test.
import org.junit.Before
// Rule: marks a JUnit rule.
import org.junit.Rule
// Test: marks a function as a test for JUnit to run.
import org.junit.Test

/**
 * Unit tests for [LoginViewModel].
 *
 * The real AuthRepository would call the internet, so a MockK mock stands in for
 * it. Each test tells the mock what to return (coEvery), calls the ViewModel,
 * then checks the resulting state (Given, When, Then).
 */
// Allows the experimental test tools to be used here.
@OptIn(ExperimentalCoroutinesApi::class)
// Creates the test class.
class LoginViewModelTest {

    // Registers the rule. "@get:" puts the label where JUnit 4 looks for it.
    @get:Rule
    // Swaps in a fake Main thread so viewModelScope works in tests.
    val mainDispatcherRule = MainDispatcherRule()

    // A fake repository.
    private val authRepository: AuthRepository = mockk()
    // The ViewModel being tested. "lateinit" because it's created in setUp().
    private lateinit var viewModel: LoginViewModel

    // Runs before every test.
    @Before
    // Sets up a fresh ViewModel.
    fun setUp() {
        // Creates a new ViewModel with the fake repository.
        // Why every time: so one test's state can't leak into the next.
        viewModel = LoginViewModel(authRepository)
    }

    // Marks this function as a test.
    @Test
    // Checks the form starts blank.
    fun `initial state has no errors and is not loading`() {
        // Compares with LoginUiState(), the blank form, which checks every field at once.
        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    // Marks this function as a test.
    @Test
    // Checks a blank student ID shows an error and nothing is sent.
    fun `blank student ID shows an error and does not call the API`() {
        // When: sign in with only spaces as the ID. Trimming turns that into "".
        viewModel.login(username = "   ", password = "Adam")

        // Then: read the state.
        val state = viewModel.uiState.value
        // Checks the "required" error is under the ID field.
        assertEquals(R.string.error_username_required, state.usernameError)
        // Checks there's no error under the name field.
        assertNull(state.passwordError)
        // Checks it isn't loading.
        assertFalse(state.isLoading)
        // Checks nothing was sent to the repository.
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // Marks this function as a test.
    @Test
    // Checks a blank first name shows an error and nothing is sent.
    fun `blank first name shows an error and does not call the API`() {
        // Signs in with an empty first name.
        viewModel.login(username = "12345678", password = "")

        // Reads the state.
        val state = viewModel.uiState.value
        // Checks the "required" error is under the name field.
        assertEquals(R.string.error_password_required, state.passwordError)
        // Checks there's no error under the ID field.
        assertNull(state.usernameError)
        // Checks nothing was sent.
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // Marks this function as a test.
    @Test
    // Checks an ID typed with an "s" (as on a student card) is rejected.
    fun `student ID with a leading s shows a format error`() {
        // Signs in with "s" in front of the ID.
        viewModel.login(username = "s12345678", password = "Adam")

        // Checks the format error is under the ID field.
        assertEquals(R.string.error_username_format, viewModel.uiState.value.usernameError)
        // Checks nothing was sent.
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // Marks this function as a test.
    @Test
    // Checks both errors show at once, instead of stopping at the first.
    fun `both fields blank shows both errors`() {
        // Signs in with both fields empty.
        viewModel.login(username = "", password = "")

        // Reads the state.
        val state = viewModel.uiState.value
        // Checks the ID error is there.
        assertEquals(R.string.error_username_required, state.usernameError)
        // Checks the name error is there too.
        assertEquals(R.string.error_password_required, state.passwordError)
    }

    // Marks this function as a test.
    @Test
    // Checks spaces are trimmed and a successful login stores the keypass.
    fun `successful login trims the input and exposes the keypass`() {
        // Given: the fake only answers for the trimmed values. If trimming broke, it would throw.
        coEvery { authRepository.login("12345678", "Adam") } returns DataResult.Success("animals")

        // When: sign in with extra spaces around both values.
        viewModel.login(username = " 12345678  ", password = "  Adam ")

        // Then: read the state.
        val state = viewModel.uiState.value
        // Checks the keypass was stored.
        assertEquals("animals", state.keypass)
        // Checks loading has stopped.
        assertFalse(state.isLoading)
        // Checks there's no server error.
        assertNull(state.loginError)
        // Checks the repository got the trimmed values, exactly once.
        coVerify(exactly = 1) { authRepository.login("12345678", "Adam") }
    }

    // Marks this function as a test.
    @Test
    // Checks wrong details show the right message.
    fun `rejected credentials show the invalid credentials message`() {
        // Tells the fake repository to say the details are wrong.
        coEvery { authRepository.login(any(), any()) } returns
            // The "wrong details" error.
            DataResult.Error(DataError.INVALID_CREDENTIALS)

        // Signs in.
        viewModel.login(username = "12345678", password = "Adam")

        // Reads the state.
        val state = viewModel.uiState.value
        // Checks the "Sign in failed" message is set.
        assertEquals(R.string.error_invalid_credentials, state.loginError)
        // Checks no keypass was stored.
        assertNull(state.keypass)
        // Checks loading has stopped.
        assertFalse(state.isLoading)
    }

    // Marks this function as a test.
    @Test
    // Checks no internet shows the right message.
    fun `network failure shows the no connection message`() {
        // Tells the fake repository to report no connection.
        coEvery { authRepository.login(any(), any()) } returns
            // The "no connection" error.
            DataResult.Error(DataError.NO_CONNECTION)

        // Signs in.
        viewModel.login(username = "12345678", password = "Adam")

        // Checks the "couldn't reach the server" message is set.
        assertEquals(R.string.error_no_connection, viewModel.uiState.value.loginError)
    }

    // Marks this function as a test.
    @Test
    // Checks the spinner shows while waiting, then stops.
    fun `state shows loading while the request is running`() = runTest {
        // Given: a result we hold back until we're ready.
        val pendingResult = CompletableDeferred<DataResult<String>>()
        // Tells the fake repository to wait for that held-back result.
        coEvery { authRepository.login(any(), any()) } coAnswers { pendingResult.await() }

        // When: sign in. The request is now waiting.
        viewModel.login(username = "12345678", password = "Adam")

        // Then: checks it's loading.
        assertTrue(viewModel.uiState.value.isLoading)

        // When: release the result.
        pendingResult.complete(DataResult.Success("animals"))
        // Lets the waiting coroutine finish.
        advanceUntilIdle()

        // Then: checks loading has stopped.
        assertFalse(viewModel.uiState.value.isLoading)
        // Checks the keypass was stored.
        assertEquals("animals", viewModel.uiState.value.keypass)
    }

    // Marks this function as a test.
    @Test
    // Checks a double tap only sends one request.
    fun `second tap while loading does not send a second request`() = runTest {
        // A result we hold back, so the first request stays waiting.
        val pendingResult = CompletableDeferred<DataResult<String>>()
        // Tells the fake repository to wait for it.
        coEvery { authRepository.login(any(), any()) } coAnswers { pendingResult.await() }

        // First tap: starts the request.
        viewModel.login(username = "12345678", password = "Adam")
        // Second tap while the first is still waiting. This should be ignored.
        viewModel.login(username = "12345678", password = "Adam")
        // Releases the result.
        pendingResult.complete(DataResult.Success("animals"))
        // Lets the coroutine finish.
        advanceUntilIdle()

        // Checks the repository was called only once.
        coVerify(exactly = 1) { authRepository.login(any(), any()) }
    }

    // Marks this function as a test.
    @Test
    // Checks the keypass is cleared after use, so the move to the Dashboard happens once.
    fun `onNavigatedToDashboard clears the keypass so navigation happens once`() {
        // Tells the fake repository to succeed.
        coEvery { authRepository.login(any(), any()) } returns DataResult.Success("animals")
        // Signs in, which stores the keypass.
        viewModel.login(username = "12345678", password = "Adam")

        // The Fragment says it has moved to the Dashboard.
        viewModel.onNavigatedToDashboard()

        // Checks the keypass is gone.
        assertNull(viewModel.uiState.value.keypass)
    }

    // Marks this function as a test.
    @Test
    // Checks typing clears only that field's error.
    fun `typing in a field clears that field's error`() {
        // Signs in with both fields empty, so both errors show.
        viewModel.login(username = "", password = "")

        // The user types in the ID field.
        viewModel.onUsernameChanged()
        // Checks the ID error is gone.
        assertNull(viewModel.uiState.value.usernameError)
        // Checks the name error is still there.
        assertEquals(R.string.error_password_required, viewModel.uiState.value.passwordError)

        // The user types in the name field.
        viewModel.onPasswordChanged()
        // Checks the name error is gone too.
        assertNull(viewModel.uiState.value.passwordError)
    }
}
