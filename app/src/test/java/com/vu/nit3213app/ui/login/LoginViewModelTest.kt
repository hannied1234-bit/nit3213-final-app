package com.vu.nit3213app.ui.login

import com.vu.nit3213app.R
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.repository.AuthRepository
import com.vu.nit3213app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [LoginViewModel].
 *
 * The real AuthRepository would call the internet, so a MockK mock stands in for
 * it. Each test tells the mock what to return (coEvery), calls the ViewModel,
 * then checks the resulting state (Given, When, Then).
 *
 * The validation tests also check that the repository was never called. Bad
 * input should be caught on the phone, and those checks prove no request
 * went out.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    // A new ViewModel for every test, so state from one test can't carry over
    // into the next. JUnit also creates a new instance of this class per test.
    @Before
    fun setUp() {
        viewModel = LoginViewModel(authRepository)
    }

    // LoginUiState() with no arguments is the blank form, so comparing against
    // it checks every field at once.
    @Test
    fun `initial state has no errors and is not loading`() {
        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    // Spaces only should count as blank, because the ViewModel trims first.
    @Test
    fun `blank student ID shows an error and does not call the API`() {
        // When
        viewModel.login(username = "   ", password = "Adam")

        // Then
        val state = viewModel.uiState.value
        assertEquals(R.string.error_username_required, state.usernameError)
        assertNull(state.passwordError)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `blank first name shows an error and does not call the API`() {
        viewModel.login(username = "12345678", password = "")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_password_required, state.passwordError)
        assertNull(state.usernameError)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // The most likely real mistake: copying the ID from a student card, where
    // it's printed with an "s" in front. The spec wants digits only.
    @Test
    fun `student ID with a leading s shows a format error`() {
        viewModel.login(username = "s12345678", password = "Adam")

        assertEquals(R.string.error_username_format, viewModel.uiState.value.usernameError)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // Confirms both fields are validated before returning, instead of
    // stopping at the first problem.
    @Test
    fun `both fields blank shows both errors`() {
        viewModel.login(username = "", password = "")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_username_required, state.usernameError)
        assertEquals(R.string.error_password_required, state.passwordError)
    }

    @Test
    fun `successful login trims the input and exposes the keypass`() {
        // Given a mock that only answers for the trimmed values. If the
        // ViewModel forgot to trim, MockK would find no match and throw.
        coEvery { authRepository.login("12345678", "Adam") } returns DataResult.Success("animals")

        // When
        viewModel.login(username = " 12345678  ", password = "  Adam ")

        // Then
        val state = viewModel.uiState.value
        assertEquals("animals", state.keypass)
        assertFalse(state.isLoading)
        assertNull(state.loginError)
        coVerify(exactly = 1) { authRepository.login("12345678", "Adam") }
    }

    @Test
    fun `rejected credentials show the invalid credentials message`() {
        coEvery { authRepository.login(any(), any()) } returns
            DataResult.Error(DataError.INVALID_CREDENTIALS)

        viewModel.login(username = "12345678", password = "Adam")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_invalid_credentials, state.loginError)
        assertNull(state.keypass)
        assertFalse(state.isLoading)
    }

    @Test
    fun `network failure shows the no connection message`() {
        coEvery { authRepository.login(any(), any()) } returns
            DataResult.Error(DataError.NO_CONNECTION)

        viewModel.login(username = "12345678", password = "Adam")

        assertEquals(R.string.error_no_connection, viewModel.uiState.value.loginError)
    }

    @Test
    fun `state shows loading while the request is running`() = runTest {
        // Given a request that does not finish until we say so
        // coAnswers runs a suspending block, and await() suspends until
        // complete() is called further down.
        val pendingResult = CompletableDeferred<DataResult<String>>()
        coEvery { authRepository.login(any(), any()) } coAnswers { pendingResult.await() }

        // When
        viewModel.login(username = "12345678", password = "Adam")

        // Then it is loading
        assertTrue(viewModel.uiState.value.isLoading)

        // When the request finishes
        pendingResult.complete(DataResult.Success("animals"))
        advanceUntilIdle()

        // Then loading stops
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("animals", viewModel.uiState.value.keypass)
    }

    // The second login() call lands while the first request is still held
    // open, which is exactly the double tap the isLoading guard stops.
    @Test
    fun `second tap while loading does not send a second request`() = runTest {
        val pendingResult = CompletableDeferred<DataResult<String>>()
        coEvery { authRepository.login(any(), any()) } coAnswers { pendingResult.await() }

        viewModel.login(username = "12345678", password = "Adam")
        viewModel.login(username = "12345678", password = "Adam")
        pendingResult.complete(DataResult.Success("animals"))
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.login(any(), any()) }
    }

    // Without this, coming back to the Login screen would see the old keypass
    // and jump straight to the Dashboard again.
    @Test
    fun `onNavigatedToDashboard clears the keypass so navigation happens once`() {
        coEvery { authRepository.login(any(), any()) } returns DataResult.Success("animals")
        viewModel.login(username = "12345678", password = "Adam")

        viewModel.onNavigatedToDashboard()

        assertNull(viewModel.uiState.value.keypass)
    }

    // Also checks that fixing one field leaves the other field's error alone.
    @Test
    fun `typing in a field clears that field's error`() {
        viewModel.login(username = "", password = "")

        viewModel.onUsernameChanged()
        assertNull(viewModel.uiState.value.usernameError)
        assertEquals(R.string.error_password_required, viewModel.uiState.value.passwordError)

        viewModel.onPasswordChanged()
        assertNull(viewModel.uiState.value.passwordError)
    }
}
